package org.stepic.droid.services

import io.reactivex.Scheduler
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.model.ViewedNotification
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.domain.notification.repository.NotificationRepository
import javax.inject.Inject

@AppSingleton
class NotificationsViewPusher
@Inject
constructor(
    private val notificationRepository: NotificationRepository,
    private val databaseFacade: DatabaseFacade,
    @BackgroundScheduler
    private val scheduler:  Scheduler
) {
    fun pushToViewedNotificationsQueue(notificationId: Long) {
        if (notificationId == 0L) return

        notificationRepository.putNotifications(notificationId, isRead = true)
                .subscribeOn(scheduler)
                .subscribe({}) {
                    databaseFacade.addToViewedNotificationsQueue(ViewedNotification(notificationId))
                }
    }
}