package org.stepic.droid.notifications

import android.support.annotation.WorkerThread
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepic.droid.notifications.model.Notification

interface StepikNotificationManager {
    @WorkerThread
    fun showNotification(notification: Notification);

    fun discardAllShownNotificationsRelatedToCourse(courseId: Long)

    fun tryOpenNotificationInstantly(notification: Notification)

    @WorkerThread
    fun showLocalNotificationRemind()

    @WorkerThread
    fun showStreakRemind()

    @WorkerThread
    fun showRegistrationRemind()

    fun showPersonalDeadlineNotification(deadline: DeadlineEntity)
}