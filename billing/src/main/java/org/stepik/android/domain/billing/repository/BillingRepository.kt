package org.stepik.android.domain.billing.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import ru.nobird.android.domain.rx.maybeFirst

interface BillingRepository {
    /**
     * Return list of Sku along with skuIds
     */
    fun getInventory(productType: String, skuIds: List<String>): Single<List<Sku>>

    /**
     * Return sku
     */
    fun getInventory(productType: String, sku: String): Maybe<Sku> =
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
                purchases.filter { it.sku in skus }
            }

    /**
     * Consumes given purchase
     */
    fun consumePurchase(purchase: Purchase): Completable
}