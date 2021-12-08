package org.stepik.android.data.billing.source

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Single

interface BillingRemoteDataSource {
    fun getInventory(productType: String, skuIds: List<String>): Single<List<SkuDetails>>
    fun getAllPurchases(productType: String): Single<List<Purchase>>
    fun consumePurchase(purchase: Purchase): Completable
}