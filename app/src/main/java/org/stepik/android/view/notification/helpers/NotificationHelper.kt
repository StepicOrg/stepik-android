package org.stepik.android.view.notification.helpers

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.notifications.model.Notification
import org.stepik.android.model.Course

interface NotificationHelper {
    fun makeSimpleNotificationBuilder(stepikNotification: org.stepic.droid.notifications.model.Notification?, justText: String, taskBuilder: TaskStackBuilder, title: String?, deleteIntent: PendingIntent = getDeleteIntent(), id: Long): NotificationCompat.Builder
    fun getDeleteIntent(courseId: Long = -1): PendingIntent
    fun addVibrationIfNeed(builder: NotificationCompat.Builder)
    fun addSoundIfNeed(builder: NotificationCompat.Builder)
    fun getTeachIntent(notification: Notification): Intent?
    fun getLicenseIntent(notification: Notification): Intent?
    fun getDefaultIntent(notification: Notification): Intent?
    fun getReviewIntent(notification: Notification): Intent?
    fun getCommentIntent(notification: Notification): Intent?
    fun getPictureByCourse(course: Course?): Bitmap
}