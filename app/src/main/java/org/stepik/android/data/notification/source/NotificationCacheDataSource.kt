package org.stepik.android.data.notification.source

import io.reactivex.Single
import org.stepic.droid.notifications.model.Notification

interface NotificationCacheDataSource {
    fun getNotificationsByCourseId(courseId: Long): Single<List<Notification>>
}