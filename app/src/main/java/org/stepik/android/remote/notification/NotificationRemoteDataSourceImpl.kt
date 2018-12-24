package org.stepik.android.remote.notification

import io.reactivex.Completable
import org.stepic.droid.web.Api
import org.stepik.android.data.notification.source.NotificationRemoteDataSource
import javax.inject.Inject

class NotificationRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : NotificationRemoteDataSource {

    override fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable =
        Completable.concat(notificationIds.map { id -> api.setReadStatusForNotificationReactive(id, isRead) })
}