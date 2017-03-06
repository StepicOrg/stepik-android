package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.NotificationTimeView
import org.stepic.droid.notifications.LocalReminder
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import java.util.concurrent.ThreadPoolExecutor

class NotificationTimePresenter(val analytic: Analytic,
                                val threadPoolExecutor: ThreadPoolExecutor,
                                val mainHandler: MainHandler,
                                val sharedPreferenceHelper: SharedPreferenceHelper,
                                val localReminder: LocalReminder) : PresenterBase<NotificationTimeView>() {
    fun tryShowNotificationSetting() {
        val isEnabled = sharedPreferenceHelper.isStreakNotificationEnabled
        val code = sharedPreferenceHelper.timeNotificationCode
        val timeNotificationString = TimeIntervalUtil.values[code]
        view?.showNotificationEnabledState(isEnabled, timeNotificationString) //todo check it in shared preferences and show
    }

    fun switchNotificationStreak(isChecked: Boolean) {
        sharedPreferenceHelper.isStreakNotificationEnabled = isChecked
        analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString() + "")
        localReminder.userChangeStateOfNotification()
        view?.hideNotificationTime(!isChecked)
    }

    fun setStreakTime(timeIntervalCode: Int) {
        sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
        val timePresentationString = TimeIntervalUtil.values[timeIntervalCode]
        localReminder.userChangeStateOfNotification()
        view?.setNewTimeInterval(timePresentationString)
    }
}
