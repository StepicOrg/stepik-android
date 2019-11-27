package org.stepik.android.data.notification.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.NotificationCategory
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.util.PagedList
import org.stepik.android.data.notification.source.NotificationCacheDataSource
import org.stepik.android.data.notification.source.NotificationRemoteDataSource
import org.stepik.android.domain.notification.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl
@Inject
constructor(
    private val notificationCacheDataSource: NotificationCacheDataSource,
    private val notificationRemoteDataSource: NotificationRemoteDataSource
) : NotificationRepository {
    override fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable =
        notificationRemoteDataSource.putNotifications(*notificationIds, isRead = isRead)

    override fun getNotificationsByCourseId(courseId: Long): Single<List<Notification>> =
        notificationCacheDataSource.getNotificationsByCourseId(courseId)

    override fun getNotifications(notificationCategory: NotificationCategory, page: Int): Single<PagedList<Notification>> =
        notificationRemoteDataSource.getNotifications(notificationCategory, page)

    override fun markNotificationAsRead(notificationCategory: NotificationCategory): Completable =
        notificationRemoteDataSource.markNotificationAsRead(notificationCategory)

    override fun getNotificationStatuses(): Single<List<NotificationStatuses>> =
        notificationRemoteDataSource.getNotificationStatuses()
}