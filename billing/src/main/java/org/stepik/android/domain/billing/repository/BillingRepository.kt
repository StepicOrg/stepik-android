package org.stepik.android.domain.billing.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst

interface BillingRepository {
    fun getInventory(productType: String, skuIds: List<String>): Single<List<SkuDetails>>

    fun getInventory(productType: String, sku: String): Maybe<SkuDetails> =
        getInventory(productType, listOf(sku))
            .maybeFirst()

    fun getAllPurchases(productType: String): Single<List<Purchase>>

    fun getAllPurchases(productType: String, skus: List<String>): Single<List<Purchase>> =
        getAllPurchases(productType)
            .map { purchases ->
                purchases.filter { it.skus.first() in skus }
            }

    fun consumePurchase(purchase: Purchase): Completable
}