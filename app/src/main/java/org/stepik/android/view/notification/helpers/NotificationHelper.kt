package org.stepik.android.view.notification.helpers

import android.app.PendingIntent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder

interface NotificationHelper {
    fun makeSimpleNotificationBuilder(stepikNotification: org.stepic.droid.notifications.model.Notification?, justText: String, taskBuilder: TaskStackBuilder, title: String?, deleteIntent: PendingIntent = getDeleteIntent(), id: Long): NotificationCompat.Builder
    fun getDeleteIntent(courseId: Long = -1): PendingIntent
    fun addVibrationIfNeed(builder: NotificationCompat.Builder)
    fun addSoundIfNeed(builder: NotificationCompat.Builder)
}