package org.stepik.android.data.billing.source

import io.reactivex.Completable
import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku

interface BillingRemoteDataSource {
    fun getInventory(productType: String, skuIds: List<String>): Single<List<Sku>>

    fun getAllPurchases(productType: String): Single<List<Purchase>>

    fun consumePurchase(purchase: Purchase): Completable
}