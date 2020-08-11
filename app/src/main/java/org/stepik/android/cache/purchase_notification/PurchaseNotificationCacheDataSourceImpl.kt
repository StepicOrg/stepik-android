package org.stepik.android.cache.purchase_notification

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.purchase_notification.dao.PurchaseNotificationDao
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.data.purchase_notification.source.PurchaseNotificationCacheDataSource
import javax.inject.Inject

class PurchaseNotificationCacheDataSourceImpl
@Inject
constructor(
    private val purchaseNotificationDao: PurchaseNotificationDao
) : PurchaseNotificationCacheDataSource {
    override fun getClosestScheduledNotification(): Maybe<PurchaseNotificationScheduled> =
        Maybe.fromCallable {
            purchaseNotificationDao.getClosestScheduledNotification()
        }

    override fun savePurchaseNotificationSchedule(purchaseNotificationScheduled: PurchaseNotificationScheduled): Completable =
        Completable.fromCallable {
            purchaseNotificationDao.insertOrReplace(purchaseNotificationScheduled)
        }
}