package org.stepik.android.view.notification

import android.content.Context
import org.stepic.droid.notifications.model.Notification

interface FcmNotificationHandler {
    fun showNotification(notification: Notification)
    fun tryOpenNotificationInstantly(context: Context, notification: Notification)
}