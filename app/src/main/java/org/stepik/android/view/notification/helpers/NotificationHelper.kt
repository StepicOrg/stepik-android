package org.stepik.android.view.notification.helpers

import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.notifications.model.Notification
import org.stepik.android.model.Course

interface NotificationHelper {
    fun makeSimpleNotificationBuilder(stepikNotification: Notification?, justText: String, taskBuilder: TaskStackBuilder, title: String?, deleteIntent: PendingIntent = getDeleteIntent(), id: Long): NotificationCompat.Builder
    fun getDeleteIntent(courseId: Long = -1): PendingIntent
    fun addVibrationIfNeed(builder: NotificationCompat.Builder)
    fun addSoundIfNeed(builder: NotificationCompat.Builder)
    fun getPictureByCourse(course: Course?): Bitmap
}