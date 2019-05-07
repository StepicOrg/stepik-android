package org.stepik.android.view.notification

import android.app.Notification

interface StepikNotifManager {
    fun scheduleNotification(id: String, millis: Long)
    fun rescheduleActiveNotifications()
    fun showNotification(id: Long, notification: Notification)
}