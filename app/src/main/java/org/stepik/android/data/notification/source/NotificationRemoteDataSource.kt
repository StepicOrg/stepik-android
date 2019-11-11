package org.stepik.android.data.notification.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.NotificationCategory
import org.stepic.droid.web.NotificationResponse
import org.stepic.droid.web.NotificationStatusesResponse

interface NotificationRemoteDataSource {
    fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable
    fun getNotificationsResponse(notificationCategory: NotificationCategory, page: Int): Single<NotificationResponse>
    fun markNotificationAsRead(notificationCategory: NotificationCategory): Completable
    fun getNotificationStatuses(): Single<NotificationStatusesResponse>
}