package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.NotificationTimeView

class NotificationTimePresenter(val analytic: Analytic) : PresenterBase<NotificationTimeView>() {
    fun tryShowNotificationSetting() {
        view?.showNotification(true, "13:00 - 14:00") //todo check it in shared preferences and show
    }

    fun switchNotifcationStreak(isChecked: Boolean) {
        //todo: save to shared prefs in main thread
        analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString() + "")
        view?.hideNotificationTime(!isChecked)
    }
}
