package org.stepik.android.presentation.profile_notification

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import javax.inject.Inject

class ProfileNotificationPresenter
@Inject
constructor(
    profileDataObservable: Observable<ProfileData>,

    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val streakNotificationDelegate: StreakNotificationDelegate,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileNotificationView>() {
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

    fun switchNotificationStreak(isChecked: Boolean) {
        sharedPreferenceHelper.isStreakNotificationEnabled = isChecked
        analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString() + "")
        streakNotificationDelegate.scheduleStreakNotification()
        view?.hideNotificationTime(!isChecked)
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