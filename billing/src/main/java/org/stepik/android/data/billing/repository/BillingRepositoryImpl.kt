package org.stepik.android.data.billing.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.repository.BillingRepository
import javax.inject.Inject

class BillingRepositoryImpl
@Inject
constructor(
    private val billingRemoteDataSourceImpl: BillingRemoteDataSource
) : BillingRepository {
    override fun getInventory(productType: String, skuIds: List<String>): Single<List<SkuDetails>> =
        billingRemoteDataSourceImpl.getInventory(productType, skuIds)

    override fun getAllPurchases(productType: String): Single<List<Purchase>> =
        billingRemoteDataSourceImpl.getAllPurchases(productType)

    override fun consumePurchase(purchase: Purchase): Completable =
        billingRemoteDataSourceImpl.consumePurchase(purchase)
}