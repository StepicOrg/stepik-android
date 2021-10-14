package org.stepic.droid.core.presenters

import android.content.res.Resources
import com.google.android.gms.tasks.Tasks
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONObject
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.DeferredAuthSplitTest
import org.stepic.droid.analytic.experiments.OnboardingSplitTestVersion2
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.configuration.analytic.*
import org.stepic.droid.core.GoogleApiChecker
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.core.presenters.contracts.SplashView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.splash.SplashScope
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.defaultLocale
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.view.routing.deeplink.BranchDeepLinkParser
import org.stepik.android.view.routing.deeplink.BranchRoute
import org.stepik.android.view.splash.notification.RemindRegistrationNotificationDelegate
import org.stepik.android.view.splash.notification.RetentionNotificationDelegate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SplashScope
class SplashPresenter
@Inject
constructor(
    @MainScheduler
    private val mainScheduler: Scheduler,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val googleApiChecker: GoogleApiChecker,
    private val analytic: Analytic,
    private val stepikDevicePoster: StepikDevicePoster,
    private val databaseFacade: DatabaseFacade,
    private val remindRegistrationNotificationDelegate: RemindRegistrationNotificationDelegate,
    private val retentionNotificationDelegate: RetentionNotificationDelegate,

    private val deferredAuthSplitTest: DeferredAuthSplitTest,
    private val onboardingSplitTestVersion2: OnboardingSplitTestVersion2,

    private val branchDeepLinkParsers: Set<@JvmSuppressWildcards BranchDeepLinkParser>
) : PresenterBase<SplashView>() {
    sealed class SplashRoute {
        object Onboarding : SplashRoute()
        object Launch : SplashRoute()
        object Home : SplashRoute()
        object Catalog : SplashRoute()
        class DeepLink(val route: BranchRoute) : SplashRoute()
    }
    companion object {
        private const val RUSSIAN_LANGUAGE_CODE = "ru"
    }

    private val locale = Resources.getSystem().configuration.defaultLocale

    private var disposable: Disposable? = null

    fun onSplashCreated(referringParams: JSONObject? = null) {
        val splashLoadingTrace = FirebasePerformance.startTrace(Analytic.Traces.SPLASH_LOADING)
        disposable = Completable
            .fromCallable {
                countNumberOfLaunches()
                checkRemoteConfigs()
                registerDeviceToPushes()
                executeLegacyOperations()
                remindRegistrationNotificationDelegate.scheduleRemindRegistrationNotification()
                retentionNotificationDelegate.scheduleRetentionNotification(shouldResetCounter = true)
                sharedPreferenceHelper.onNewSession()
                if (onboardingSplitTestVersion2.currentGroup == OnboardingSplitTestVersion2.Group.None && locale.language == RUSSIAN_LANGUAGE_CODE) {
                    sharedPreferenceHelper.afterOnboardingPassed()
                    sharedPreferenceHelper.setPersonalizedOnboardingWasShown()
                }
                if (sharedPreferenceHelper.isEverLogged) {
                    sharedPreferenceHelper.setPersonalizedOnboardingWasShown()
                }
            }
            .andThen(resolveSplashRoute(referringParams))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doFinally {
                splashLoadingTrace.stop()
            }
            .subscribeBy(
                onError = emptyOnErrorStub,
                onSuccess = {
                    when (it) {
                        SplashRoute.Onboarding  -> view?.onShowOnboarding()
                        SplashRoute.Launch      -> view?.onShowLaunch()
                        SplashRoute.Home        -> view?.onShowHome()
                        SplashRoute.Catalog     -> view?.onShowCatalog()
                        is SplashRoute.DeepLink -> view?.onDeepLinkRoute(it.route)
                        else -> throw IllegalStateException("It is not reachable")
                    }
                }
            )
    }

    private fun resolveSplashRoute(referringParams: JSONObject?): Single<SplashRoute> = // todo move to interactor
        resolveBranchRoute(referringParams)
            .map { SplashRoute.DeepLink(it) as SplashRoute }
            .onErrorReturn {
                val isLogged = sharedPreferenceHelper.authResponseFromStore != null
                val isOnboardingNotPassedYet = resolveIsOnboardingNotPassedYet()
//                val isDeferredAuth = deferredAuthSplitTest.currentGroup.isDeferredAuth && !deferredAuthSplitTest.currentGroup.isCanDismissLaunch
                when {
                    isOnboardingNotPassedYet -> SplashRoute.Onboarding
                    isLogged -> SplashRoute.Home
//                    isDeferredAuth -> SplashRoute.Catalog
                    else -> SplashRoute.Launch
                }
            }

    private fun resolveIsOnboardingNotPassedYet(): Boolean {
        // Guard so that this works as usual for every locale except RU
        if (locale.language != RUSSIAN_LANGUAGE_CODE) {
            return sharedPreferenceHelper.isOnboardingNotPassedYet
        }
        return when (onboardingSplitTestVersion2.currentGroup) {
            OnboardingSplitTestVersion2.Group.Control ->
                sharedPreferenceHelper.isOnboardingNotPassedYet
            OnboardingSplitTestVersion2.Group.Personalized ->
                !sharedPreferenceHelper.isPersonalizedOnboardingWasShown
            OnboardingSplitTestVersion2.Group.None ->
                false
            OnboardingSplitTestVersion2.Group.ControlPersonalized ->
                sharedPreferenceHelper.isOnboardingNotPassedYet || !sharedPreferenceHelper.isPersonalizedOnboardingWasShown
        }
    }


    private fun resolveBranchRoute(referringParams: JSONObject?): Single<BranchRoute> =
        if (referringParams == null) {
            Single.error(IllegalArgumentException("Params shouldn't be null"))
        } else {
            Single.fromCallable {
                branchDeepLinkParsers.forEach { parser ->
                    val route = parser.parseBranchDeepLink(referringParams)
                    if (route != null) {
                        return@fromCallable route
                    }
                }
                return@fromCallable null
            }
        }

    override fun detachView(view: SplashView) {
        super.detachView(view)
        disposable?.dispose()
        disposable = null
    }

    private fun checkRemoteConfigs() {
        if (googleApiChecker.checkPlayServices()) {
            val remoteConfigTask = firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    analytic.reportEvent(Analytic.RemoteConfig.FETCHED_SUCCESSFUL)
                } else {
                    analytic.reportEvent(Analytic.RemoteConfig.FETCHED_UNSUCCESSFUL)
                }
                logRemoteConfig()
            }
            try {
                Tasks.await(remoteConfigTask)
            } catch (exception: Exception) {
                // no op
            }
        }
    }

    private fun logRemoteConfig() {
        analytic.reportUserProperty(MinDelayRateDialogUserProperty(firebaseRemoteConfig[RemoteConfig.MIN_DELAY_RATE_DIALOG_SEC].asLong()))
        analytic.reportUserProperty(ShowStreakDialogAfterLoginUserProperty(firebaseRemoteConfig[RemoteConfig.SHOW_STREAK_DIALOG_AFTER_LOGIN].asBoolean()))
        analytic.reportUserProperty(AdaptiveCoursesUserProperty(firebaseRemoteConfig[RemoteConfig.ADAPTIVE_COURSES].asString()))
        analytic.reportUserProperty(AdaptiveBackendUrlUserProperty(firebaseRemoteConfig[RemoteConfig.ADAPTIVE_BACKEND_URL].asString()))
        analytic.reportUserProperty(LocalSubmissionsEnabledUserProperty(firebaseRemoteConfig[RemoteConfig.IS_LOCAL_SUBMISSIONS_ENABLED].asBoolean()))
        analytic.reportUserProperty(SearchQueryParamsUserProperty(firebaseRemoteConfig[RemoteConfig.SEARCH_QUERY_PARAMS_ANDROID].asString()))
        analytic.reportUserProperty(NewHomeScreenEnabledUserProperty(firebaseRemoteConfig[RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED].asBoolean()))
        analytic.reportUserProperty(PersonalizedOnboardingCourseListsUserProperty(firebaseRemoteConfig[RemoteConfig.PERSONALIZED_ONBOARDING_COURSE_LISTS].asString()))
        analytic.reportUserProperty(CourseRevenueAvailableUserProperty(firebaseRemoteConfig[RemoteConfig.IS_COURSE_REVENUE_AVAILABLE_ANDROID].asBoolean()))
    }

    private fun countNumberOfLaunches() {
        val numberOfLaunches = sharedPreferenceHelper.incrementNumberOfLaunches()
        //after first increment it is 0, because of default value is -1.
        if (numberOfLaunches <= 0) {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Launch.FIRST_TIME)
        }
        if (numberOfLaunches < AppConstants.LAUNCHES_FOR_EXPERT_USER) {
            analytic.reportEvent(Analytic.Interaction.START_SPLASH, numberOfLaunches.toString() + "")
        } else {
            analytic.reportEvent(Analytic.Interaction.START_SPLASH_EXPERT, numberOfLaunches.toString() + "")
        }
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Launch.SESSION_START)
    }


    private fun registerDeviceToPushes() {
        if (!sharedPreferenceHelper.isGcmTokenOk && googleApiChecker.checkPlayServices()) {
            stepikDevicePoster.registerDevice()
        }
    }

    private fun executeLegacyOperations() {
        if (sharedPreferenceHelper.isOnboardingNotPassedYet) {
            databaseFacade.dropOnlyCourseTable() //v11 bug, when slug was not cached. We can remove it, when all users will have v1.11 or above. (flavour problem)
            sharedPreferenceHelper.afterScheduleAdded()
            sharedPreferenceHelper.afterNeedDropCoursesIn114()
        } else if (!sharedPreferenceHelper.isScheduleAdded) {
            databaseFacade.dropOnlyCourseTable()
            sharedPreferenceHelper.afterScheduleAdded()
            sharedPreferenceHelper.afterNeedDropCoursesIn114()
        } else if (sharedPreferenceHelper.isNeedDropCoursesIn114) {
            databaseFacade.dropOnlyCourseTable()
            sharedPreferenceHelper.afterNeedDropCoursesIn114()
        }
    }
}
