package org.stepik.android.remote.billing

import io.reactivex.Scheduler
import io.reactivex.Single
import org.solovyev.android.checkout.*
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.exception.BillingNotSupportedException
import org.stepik.android.view.injection.billing.SystemCheckout
import java.lang.Exception
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
                request.loadSkus(productType, skuIds)
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
}