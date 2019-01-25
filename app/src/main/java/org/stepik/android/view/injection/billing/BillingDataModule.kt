package org.stepik.android.view.injection.billing

import dagger.Binds
import dagger.Module
import org.stepik.android.data.billing.repository.BillingRepositoryImpl
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.remote.billing.BillingRemoteDataSourceImpl

@Module
abstract class BillingDataModule {
    @Binds
    internal abstract fun bindBillingRepository(billingRepositoryImpl: BillingRepositoryImpl): BillingRepository

    @Binds
    internal abstract fun bindBillingRemoteDataSource(billingRemoteDataSourceImpl: BillingRemoteDataSourceImpl): BillingRemoteDataSource
}