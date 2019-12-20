package org.stepik.android.remote.billing

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Purchases
import org.solovyev.android.checkout.RequestListener
import org.solovyev.android.checkout.Sku
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.billing.extension.consumeRx
import org.stepik.android.domain.billing.extension.onReady
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.exception.BillingNotSupportedException
import org.stepik.android.view.injection.billing.SystemCheckout
import javax.inject.Inject

class BillingRemoteDataSourceImpl
@Inject
constructor(
    private val billing: Billing,

    @SystemCheckout
    private val checkout: Checkout,

    @MainScheduler
    private val mainScheduler: Scheduler
) : BillingRemoteDataSource {
    override fun getInventory(productType: String, skuIds: List<String>): Single<List<Sku>> =
        Single
            .create<List<Sku>> { emitter ->
                val request = Inventory.Request.create()
                request.loadAllPurchases()
                request.loadSkus(productType, skuIds.distinct())
                checkout.loadInventory(request) { products ->
                    if (!emitter.isDisposed) {
                        val product = products[productType]
                        if (product.supported) {
                            emitter.onSuccess(product.skus)
                        } else {
                            emitter.onError(BillingNotSupportedException())
                        }
                    }
                }
            }
            .subscribeOn(mainScheduler)

    override fun getAllPurchases(productType: String): Single<List<Purchase>> =
        Single.create { emitter ->
            billing
                .newRequestsBuilder()
                .create()
                .getAllPurchases(productType, object : RequestListener<Purchases> {
                    override fun onSuccess(purchases: Purchases) {
                        if (!emitter.isDisposed) {
                            emitter.onSuccess(purchases.list)
                        }
                    }

                    override fun onError(response: Int, e: Exception) {
                        if (!emitter.isDisposed)  {
                            emitter.onError(e)
                        }
                    }
                })
        }

    override fun consumePurchase(purchase: Purchase): Completable =
        checkout
            .onReady()
            .flatMapCompletable { requests ->
                requests.consumeRx(purchase.token)
            }
            .subscribeOn(mainScheduler)
}