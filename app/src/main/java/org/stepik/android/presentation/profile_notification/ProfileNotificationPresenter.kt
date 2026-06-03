package org.stepik.android.presentation.profile_notification

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepik.android.domain.feature.interactor.FeaturesInteractor
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import javax.inject.Inject

class ProfileNotificationPresenter
@Inject
constructor(
    profileDataObservable: Observable<ProfileData>,

    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val featuresInteractor: FeaturesInteractor,
    private val profileRepository: ProfileRepository,
    private val profileSubject: PublishSubject<Profile>,
    private val streakNotificationDelegate: StreakNotificationDelegate,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileNotificationView>() {
    private var profile: Profile? = null
    private var isMarketingProfileLoading: Boolean = false
    private var isMarketingUpdateInProgress: Boolean = false

    private var profileData: ProfileData? = null
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        compositeDisposable += profileDataObservable
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = ::profileData::set
            )
    }

    fun tryShowNotificationSetting() {
        val isEnabled = sharedPreferenceHelper.isStreakNotificationEnabled
        val code = sharedPreferenceHelper.timeNotificationCode
        val timeNotificationString = TimeIntervalUtil.values[code]
        view?.showNotificationEnabledState(isEnabled, timeNotificationString)
    }

    fun tryShowMarketingNotificationSetting() {
        if (!featuresInteractor.isAuthMarketingAgreementEnabledCached()) {
            view?.hideMarketingNotification()
            return
        }

        profile?.let {
            view?.showMarketingNotificationState(
                subscribedForMarketing = it.subscribedForMarketing == true,
                isUpdating = isMarketingUpdateInProgress
            )
            return
        }

        if (isMarketingProfileLoading) {
            return
        }

        isMarketingProfileLoading = true
        compositeDisposable += profileRepository
            .getProfile()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { profile ->
                    isMarketingProfileLoading = false
                    this.profile = profile
                    view?.showMarketingNotificationState(
                        subscribedForMarketing = profile.subscribedForMarketing == true,
                        isUpdating = isMarketingUpdateInProgress
                    )
                },
                onError = {
                    isMarketingProfileLoading = false
                    view?.hideMarketingNotification()
                }
            )
    }

    fun tryShowNotificationSettingAfterGrantingPermission() {
        sharedPreferenceHelper.isStreakNotificationEnabled = true
        tryShowNotificationSetting()
    }

    fun switchNotificationStreak(isChecked: Boolean) {
        sharedPreferenceHelper.isStreakNotificationEnabled = isChecked
        analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString() + "")
        streakNotificationDelegate.scheduleStreakNotification()
        view?.hideNotificationTime(!isChecked)
    }

    fun switchMarketingNotification(isChecked: Boolean) {
        val currentProfile = profile ?: return
        if (isMarketingUpdateInProgress) {
            return
        }

        val previousSubscribed = currentProfile.subscribedForMarketing == true
        profile = currentProfile.copy(subscribedForMarketing = isChecked)
        isMarketingUpdateInProgress = true
        view?.showMarketingNotificationState(isChecked, isUpdating = true)

        compositeDisposable += profileRepository
            .saveProfile(currentProfile.copy(subscribedForMarketing = isChecked))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { updatedProfile ->
                    profile = updatedProfile
                    isMarketingUpdateInProgress = false
                    profileSubject.onNext(updatedProfile)
                    view?.showMarketingNotificationState(
                        subscribedForMarketing = updatedProfile.subscribedForMarketing == true,
                        isUpdating = false
                    )
                },
                onError = {
                    profile = currentProfile
                    isMarketingUpdateInProgress = false
                    view?.showMarketingNotificationState(previousSubscribed, isUpdating = false)
                    view?.showMarketingNotificationUpdateFailed()
                }
            )
    }

    fun setStreakTime(timeIntervalCode: Int) {
        sharedPreferenceHelper.isStreakNotificationEnabled = true
        sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
        val timePresentationString = TimeIntervalUtil.values[timeIntervalCode]
        streakNotificationDelegate.scheduleStreakNotification()
        view?.setNewTimeInterval(timePresentationString)
    }

    override fun attachView(view: ProfileNotificationView) {
        super.attachView(view)
        view.setState(profileData)
    }
}