package org.stepik.android.data.purchase_notification.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled

interface PurchaseNotificationCacheDataSource {
    fun getClosestTimeStamp(): Single<Long>
    fun getClosestScheduledNotification(): Maybe<PurchaseNotificationScheduled>
    fun getClosestExpiredScheduledNotification(): Maybe<PurchaseNotificationScheduled>
    fun savePurchaseNotificationSchedule(purchaseNotificationScheduled: PurchaseNotificationScheduled): Completable
}