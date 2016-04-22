package org.stepic.droid.notifications

import org.stepic.droid.notifications.model.Notification

interface  INotificationManager {
    /**
     * worker thread
     */
    fun showNotification(notification : Notification);

    fun discardAllNotifications(courseId:Long)
}