package org.stepik.android.domain.billing.repository

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku

interface BillingRepository {
    /**
     * Return list of Sku along with skuIds
     */
    fun getInventory(productType: String, skuIds: List<String>): Single<List<Sku>>

    /**
     * Return all available purchases (not consumed)
     */
    fun getAllPurchases(productType: String): Single<List<Purchase>>
}