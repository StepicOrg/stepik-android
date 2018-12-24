package org.stepik.android.domain.notification.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.notifications.model.Notification

interface NotificationRepository {
    fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable

    fun getNotificationsByCourseId(courseId: Long): Single<List<Notification>>
}