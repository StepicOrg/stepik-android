package org.stepic.droid.notifications

import android.support.annotation.WorkerThread
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepic.droid.notifications.model.Notification

interface StepikNotificationManager {
    companion object {
        const val SHOW_REGISTRATION_NOTIFICATION = "show_registration_notification"
        const val SHOW_NEW_USER_NOTIFICATION = "show_new_user_notification"

        const val SHOW_STREAK_NOTIFICATION = "show_streak_notification"

        const val SHOW_RETENTION_NOTIFICATION = "show_retention_notification"
    }

    @WorkerThread
    fun showNotification(notification: Notification)

    fun discardAllShownNotificationsRelatedToCourse(courseId: Long)

    fun tryOpenNotificationInstantly(notification: Notification)

    @WorkerThread
    fun showLocalNotificationRemind()

    @WorkerThread
    fun showStreakRemind()

    @WorkerThread
    fun showRegistrationRemind()

    @WorkerThread
    fun showRetentionNotification()

    fun showPersonalDeadlineNotification(deadline: DeadlineEntity)
}