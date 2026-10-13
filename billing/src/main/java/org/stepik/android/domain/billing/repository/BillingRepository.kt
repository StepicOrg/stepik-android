package org.stepik.android.domain.billing.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.ProductDetails
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst

interface BillingRepository {
    /**
     * Return list of ProductDetails along with skuIds
     */
    fun getInventory(productType: String, skuIds: List<String>): Single<List<ProductDetails>>

    /**
     * Return single ProductDetails
     */
    fun getInventory(productType: String, sku: String): Maybe<ProductDetails> =
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
                purchases.filter { purchase -> purchase.products.any { it in skus } }
            }

    /**
     * Consumes given purchase
     */
    fun consumePurchase(purchase: Purchase): Completable
}