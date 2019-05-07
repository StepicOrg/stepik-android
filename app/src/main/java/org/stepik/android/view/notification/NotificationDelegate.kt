package org.stepik.android.view.notification

import android.app.Notification

abstract class NotificationDelegate(
    val id: String,
    val notificationManager: StepikNotifManager
){

    abstract fun onNeedShowNotification()

    fun scheduleNotificationAt(timestamp: Long) {
        notificationManager.scheduleNotification(id, timestamp)
    }

    fun showNotification(id: Long, notification: Notification) {
        notificationManager.showNotification(id, notification)
    }
}