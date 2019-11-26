package org.stepik.android.data.notification.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.NotificationCategory
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.util.PagedList

interface NotificationRemoteDataSource {
    fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable
    fun getNotifications(notificationCategory: NotificationCategory, page: Int): Single<PagedList<Notification>>
    fun markNotificationAsRead(notificationCategory: NotificationCategory): Completable
    fun getNotificationStatuses(): Single<List<NotificationStatuses>>
}