package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.NotificationTimeView
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepik.android.view.notification.delegate.StreakDelegate
import javax.inject.Inject

class StreakPresenter
@Inject constructor(
        private val analytic: Analytic,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val streakDelegate: StreakDelegate) : PresenterBase<NotificationTimeView>() {

    fun tryShowNotificationSetting() {
        val isEnabled = sharedPreferenceHelper.isStreakNotificationEnabled
        val code = sharedPreferenceHelper.timeNotificationCode
        val timeNotificationString = TimeIntervalUtil.values[code]
        view?.showNotificationEnabledState(isEnabled, timeNotificationString) //todo check it in shared preferences and show
    }

    fun switchNotificationStreak(isChecked: Boolean) {
        sharedPreferenceHelper.isStreakNotificationEnabled = isChecked
        analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString() + "")
        streakDelegate.scheduleStreakNotification()
        view?.hideNotificationTime(!isChecked)
    }

    fun setStreakTime(timeIntervalCode: Int) {
        sharedPreferenceHelper.isStreakNotificationEnabled = true
        sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
        val timePresentationString = TimeIntervalUtil.values[timeIntervalCode]
        streakDelegate.scheduleStreakNotification()
        view?.setNewTimeInterval(timePresentationString)
    }
}
