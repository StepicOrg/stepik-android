package org.stepik.android.cache.notification

import io.reactivex.Single
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.notification.source.NotificationCacheDataSource
import javax.inject.Inject

class NotificationCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : NotificationCacheDataSource {

    override fun getNotificationsByCourseId(courseId: Long): Single<List<Notification>> =
        Single.fromCallable {
            databaseFacade.getAllNotificationsOfCourse(courseId)
        }
}