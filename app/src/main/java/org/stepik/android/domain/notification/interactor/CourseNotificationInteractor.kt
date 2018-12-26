package org.stepik.android.domain.notification.interactor

import io.reactivex.Completable
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.notification.repository.NotificationRepository
import javax.inject.Inject

class CourseNotificationInteractor
@Inject
constructor(
    private val notificationRepository: NotificationRepository
) {

    fun markCourseNotificationsAsRead(courseId: Long): Completable =
        notificationRepository
            .getNotificationsByCourseId(courseId)
            .flatMapCompletable { notifications ->
                notificationRepository
                    .putNotifications(*notifications.mapToLongArray { it.id ?: 0 }, isRead = true)
            }

}