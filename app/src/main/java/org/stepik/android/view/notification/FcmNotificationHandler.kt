package org.stepik.android.view.notification

import org.stepic.droid.notifications.model.Notification

interface FcmNotificationHandler {
    fun showNotification(notification: Notification)
    fun tryOpenNotificationInstantly(notification: Notification)
}