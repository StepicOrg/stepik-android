package org.stepic.droid.notifications

import android.support.annotation.WorkerThread
import org.stepic.droid.notifications.model.Notification

interface INotificationManager {
    /**
     * worker thread
     */
    fun showNotification(notification: Notification);

    fun discardAllNotifications(courseId: Long)

    fun tryOpenNotificationInstantly(notification: Notification)

    @WorkerThread
    fun showLocalNotificationRemind()

    @WorkerThread
    fun showStreakRemind()
}