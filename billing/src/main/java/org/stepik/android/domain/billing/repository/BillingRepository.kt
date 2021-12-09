package org.stepik.android.domain.billing.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst

interface BillingRepository {
    /**
     * Return list of SkuDetails along with skuIds
     */
    fun getInventory(productType: String, skuIds: List<String>): Single<List<SkuDetails>>

    /**
     * Return single SkuDetails
     */
    fun getInventory(productType: String, sku: String): Maybe<SkuDetails> =
        getInventory(productType, listOf(sku))
            .maybeFirst()

    /**
     * Return all available purchases (not consumed)
     */
    fun getAllPurchases(productType: String): Single<List<Purchase>>

    /**
     * Returns all available purchases with skus
     */
    fun getAllPurchases(productType: String, skus: List<String>): Single<List<Purchase>> =
        getAllPurchases(productType)
            .map { purchases ->
                purchases.filter { it.skus.first() in skus }
            }

    /**
     * Consumes given purchase
     */
    fun consumePurchase(purchase: Purchase): Completable
}