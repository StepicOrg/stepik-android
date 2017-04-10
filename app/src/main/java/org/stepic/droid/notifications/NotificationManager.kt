package org.stepic.droid.notifications

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.notifications.model.Notification

interface NotificationManager {
    @WorkerThread
    fun showNotification(notification: Notification);

    fun discardAllShownNotificationsRelatedToCourse(courseId: Long)

    fun tryOpenNotificationInstantly(notification: Notification)

    @WorkerThread
    fun showLocalNotificationRemind()

    @WorkerThread
    fun showStreakRemind()

    @MainThread
    fun showAllRescheduledNotifications()
}