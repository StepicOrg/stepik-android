package org.stepik.android.domain.purchase_notification.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled

interface PurchaseNotificationRepository {
    fun getClosestScheduledNotification(): Maybe<PurchaseNotificationScheduled>
    fun savePurchaseNotificationSchedule(purchaseNotificationScheduled: PurchaseNotificationScheduled): Completable
}