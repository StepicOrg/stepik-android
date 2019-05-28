package org.stepik.android.view.notification

import android.app.Notification

interface StepikNotificationManager {
    fun scheduleNotification(id: String, millis: Long)
    fun rescheduleActiveNotification(id: String)
    fun showNotification(id: Long, notification: Notification)
}