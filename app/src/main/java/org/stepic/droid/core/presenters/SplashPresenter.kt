package org.stepic.droid.core.presenters

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.GoogleApiChecker
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.core.presenters.contracts.SplashView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.di.splash.SplashScope
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.RxOptional
import org.stepic.droid.web.Api
import javax.inject.Inject

@SplashScope
class SplashPresenter
@Inject
constructor(
        @MainScheduler
        private val mainScheduler: Scheduler,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        private val api: Api,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
        private val googleApiChecker: GoogleApiChecker,
        private val analytic: Analytic,
        private val stepikDevicePoster: StepikDevicePoster,
        private val databaseFacade: DatabaseFacade
) : PresenterBase<SplashView>() {

    private var disposable: Disposable? = null

    fun onSplashCreated() {
        disposable = Observable
                .fromCallable {
                    updateProfile()
                    checkRemoteConfigs()
                    countNumberOfLaunches()
                    registerDeviceToPushes()
                    executeLegacyOperations()
                }
                .map {
                    RxOptional(sharedPreferenceHelper.authResponseFromStore)
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.value == null) {
                        view?.onShowLaunch()
                    } else {
                        view?.onShowHome()
                    }
                }

    }


    override fun detachView(view: SplashView) {
        super.detachView(view)
        disposable?.dispose()
        disposable = null
    }

    private fun updateProfile() {
        val profile = try {
            api.userProfile.execute().body()?.getProfile() ?: return
        } catch (exception: Exception) {
            return
        }
        sharedPreferenceHelper.storeProfile(profile)
    }

    private fun checkRemoteConfigs() {
        if (googleApiChecker.checkPlayServices()) {
            firebaseRemoteConfig.fetch().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    analytic.reportEvent(Analytic.RemoteConfig.FETCHED_SUCCESSFUL)
                    firebaseRemoteConfig.activateFetched()
                } else {
                    analytic.reportEvent(Analytic.RemoteConfig.FETCHED_UNSUCCESSFUL)
                }
            }
        }
    }

    private fun countNumberOfLaunches() {
        val numberOfLaunches = sharedPreferenceHelper.incrementNumberOfLaunches()
        //after first increment it is 0, because of default value is -1.
        if (numberOfLaunches <= 0) {
            analytic.reportEvent(Analytic.System.FIRST_LAUNCH_AFTER_INSTALL)
        }
        if (numberOfLaunches < AppConstants.LAUNCHES_FOR_EXPERT_USER) {
            analytic.reportEvent(Analytic.Interaction.START_SPLASH, numberOfLaunches.toString() + "")
        } else {
            analytic.reportEvent(Analytic.Interaction.START_SPLASH_EXPERT, numberOfLaunches.toString() + "")
        }
    }


    private fun registerDeviceToPushes() {
        if (!sharedPreferenceHelper.isGcmTokenOk && googleApiChecker.checkPlayServices()) {
            stepikDevicePoster.registerDevice()
        }
    }

    private fun executeLegacyOperations() {
        if (sharedPreferenceHelper.isFirstTime) {
            databaseFacade.dropOnlyCourseTable() //v11 bug, when slug was not cached. We can remove it, when all users will have v1.11 or above. (flavour problem)
            sharedPreferenceHelper.afterFirstTime()
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
