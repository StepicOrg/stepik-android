package org.stepik.android.data.notification.source

import io.reactivex.Completable

interface NotificationRemoteDataSource {
    fun putNotifications(vararg notificationIds: Long, isRead: Boolean): Completable
}