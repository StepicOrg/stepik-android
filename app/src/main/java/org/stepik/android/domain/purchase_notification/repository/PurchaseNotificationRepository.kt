package org.stepik.android.domain.purchase_notification.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled

interface PurchaseNotificationRepository {
    fun getClosestTimeStamp(): Single<Long>
    fun getClosestScheduledNotification(): Maybe<PurchaseNotificationScheduled>
    fun getClosestExpiredScheduledNotification(): Maybe<PurchaseNotificationScheduled>
    fun savePurchaseNotificationSchedule(purchaseNotificationScheduled: PurchaseNotificationScheduled): Completable
}