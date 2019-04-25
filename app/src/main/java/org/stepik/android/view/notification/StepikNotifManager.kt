package org.stepik.android.view.notification

interface StepikNotifManager {
    fun scheduleNotification(id: String, millis: Long)
    fun rescheduleActiveNotifications()
}