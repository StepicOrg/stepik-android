package org.stepik.android.view.injection.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import dagger.BindsInstance
import dagger.Component
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.repository.BillingRepository

@Component(modules = [BillingModule::class, BillingDataModule::class])
@BillingSingleton
interface BillingComponent {
    @Component.Builder
    interface Builder {
        fun build(): BillingComponent

        @BindsInstance
        fun context(context: Context): Builder
    }

    val billingClient: BillingClient
    val purchaseListenerBehaviorRelay: PublishRelay<Pair<BillingResult, List<Purchase>?>>
    val billingRemoteDataSource: BillingRemoteDataSource
    val billingRepository: BillingRepository
}