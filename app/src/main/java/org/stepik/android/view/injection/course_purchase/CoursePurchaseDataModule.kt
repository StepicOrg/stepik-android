package org.stepik.android.view.injection.course_purchase

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.course_purchase.BillingPurchasePayloadCacheDataSourceImpl
import org.stepik.android.cache.course_purchase.dao.BillingPurchasePayloadDao
import org.stepik.android.data.course_purchase.repository.BillingPurchasePayloadRepositoryImpl
import org.stepik.android.data.course_purchase.source.BillingPurchasePayloadCacheDataSource
import org.stepik.android.domain.course_purchase.repository.BillingPurchasePayloadRepository

@Module
abstract class CoursePurchaseDataModule {
    @Binds
    internal abstract fun bindBillingPurchasePayloadRepository(
        billingPurchasePayloadRepositoryImpl: BillingPurchasePayloadRepositoryImpl
    ): BillingPurchasePayloadRepository

    @Binds
    internal abstract fun bindBillingPurchasePayloadCacheDataSource(
        billingPurchaseCacheDataSourceImpl: BillingPurchasePayloadCacheDataSourceImpl
    ): BillingPurchasePayloadCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideBillingPurchasePayloadDao(appDatabase: AppDatabase): BillingPurchasePayloadDao =
            appDatabase.billingPurchasePayloadDao()
    }
}