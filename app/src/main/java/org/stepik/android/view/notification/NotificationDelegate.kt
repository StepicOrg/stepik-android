package org.stepik.android.view.notification

import android.app.Notification

abstract class NotificationDelegate(
    val id: String,
    val notificationManager: StepikNotifManager
){

    abstract fun onNeedShowNotification()

    abstract fun scheduleNotification()

    fun rescheduleNotification() {
        notificationManager.rescheduleActiveNotification(id)
    }

    protected fun scheduleNotificationAt(timestamp: Long) {
        notificationManager.scheduleNotification(id, timestamp)
    }

    protected fun showNotification(id: Long, notification: Notification) {
        notificationManager.showNotification(id, notification)
    }
}