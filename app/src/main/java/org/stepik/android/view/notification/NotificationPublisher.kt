package org.stepik.android.view.notification

interface NotificationPublisher {
    fun onNeedShowNotificationWithId(id: String)
}