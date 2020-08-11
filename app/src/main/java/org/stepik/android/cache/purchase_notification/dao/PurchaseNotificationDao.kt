package org.stepik.android.cache.purchase_notification.dao

import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled

interface PurchaseNotificationDao : IDao<PurchaseNotificationScheduled> {
    fun getClosestScheduledNotification(): PurchaseNotificationScheduled?
}