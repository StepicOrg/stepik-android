package org.stepik.android.data.purchase_notification.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.data.purchase_notification.source.PurchaseNotificationCacheDataSource
import org.stepik.android.domain.purchase_notification.repository.PurchaseNotificationRepository
import javax.inject.Inject

class PurchaseNotificationRepositoryImpl
@Inject
constructor(
    private val purchaseNotificationCacheDataSource: PurchaseNotificationCacheDataSource
) : PurchaseNotificationRepository {
    override fun getClosestScheduledNotification(): Maybe<PurchaseNotificationScheduled> =
        purchaseNotificationCacheDataSource.getClosestScheduledNotification()

    override fun savePurchaseNotificationSchedule(purchaseNotificationScheduled: PurchaseNotificationScheduled): Completable =
        purchaseNotificationCacheDataSource.savePurchaseNotificationSchedule(purchaseNotificationScheduled)
}