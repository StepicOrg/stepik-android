package org.stepik.android.view.injection.purchase_notification

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.purchase_notification.PurchaseNotificationCacheDataSourceImpl
import org.stepik.android.data.purchase_notification.repository.PurchaseNotificationRepositoryImpl
import org.stepik.android.data.purchase_notification.source.PurchaseNotificationCacheDataSource
import org.stepik.android.domain.purchase_notification.repository.PurchaseNotificationRepository

@Module
abstract class PurchaseNotificationDataModule {
    @Binds
    internal abstract fun bindPurchaseNotificationRepository(
        purchaseNotificationRepositoryImpl: PurchaseNotificationRepositoryImpl
    ): PurchaseNotificationRepository

    @Binds
    internal abstract fun bindPurchaseNotificationCacheDataSource(
        purchaseNotificationCacheDataSourceImpl: PurchaseNotificationCacheDataSourceImpl
    ): PurchaseNotificationCacheDataSource
}