package org.stepik.android.remote.notification

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.NotificationCategory
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.util.PagedList
import org.stepik.android.data.notification.source.NotificationRemoteDataSource
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.notification.model.NotificationRequest
import org.stepik.android.remote.notification.model.NotificationResponse
import org.stepik.android.remote.notification.model.NotificationStatusesResponse
import org.stepik.android.remote.notification.service.NotificationService
import javax.inject.Inject

class NotificationRemoteDataSourceImpl
@Inject
constructor(
    private val notificationService: NotificationService
) : NotificationRemoteDataSource {

    override fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable =
        Completable.concat(notificationIds.map { id ->
            val notification = Notification()
            notification.isUnread = !isRead
            notificationService.putNotification(id, NotificationRequest(notification))
        })

    override fun getNotifications(notificationCategory: NotificationCategory, page: Int): Single<PagedList<Notification>> =
        notificationService
            .getNotifications(page, type = getNotificationCategoryString(notificationCategory))
            .map { it.toPagedList(NotificationResponse::notifications) }

    override fun markNotificationAsRead(notificationCategory: NotificationCategory): Completable =
        notificationService
            .markNotificationAsRead(getNotificationCategoryString(notificationCategory))

    override fun getNotificationStatuses(): Single<List<NotificationStatuses>> =
        notificationService
            .getNotificationStatuses()
            .map(NotificationStatusesResponse::notificationStatuses)

    private fun getNotificationCategoryString(notificationCategory: NotificationCategory): String? =
        if (notificationCategory === NotificationCategory.all) {
            null
        } else {
            notificationCategory.name
        }
}