package org.stepik.android.view.notification

abstract class NotificationDelegate(
    val id: String,
    val notificationManager: StepikNotifManager
){

    abstract fun onNeedShowNotification()

    protected fun scheduleNotificationAt(timestamp: Long) {
        notificationManager.scheduleNotification(id, timestamp)
    }
}