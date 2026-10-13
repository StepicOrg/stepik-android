package org.stepik.android.remote.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.exceptions.Exceptions
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.domain.billing.exception.BillingNotSupportedException
import org.stepik.android.view.injection.billing.BillingSingleton
import ru.nobird.android.view.injection.base.RxScheduler
import javax.inject.Inject

@BillingSingleton
class BillingRemoteDataSourceImpl
@Inject
constructor(
    @BillingSingleton
    private val billingClient: BillingClient,

    @RxScheduler.Background
    private val backgroundScheduler: Scheduler
) : BillingRemoteDataSource {

    override fun getInventory(productType: String, skuIds: List<String>): Single<List<ProductDetails>> =
        Single
            .create<List<ProductDetails>> { emitter ->
                // Billing rejects empty product lists, including when all prices are cached.
                if (skuIds.isEmpty()) {
                    emitter.onSuccess(emptyList())
                    return@create
                }

                val productParams = QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(skuIds.map { skuId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuId)
                            .setProductType(productType)
                            .build()
                    })
                    .build()

                try {
                    billingClient.queryProductDetailsAsync(productParams) { billingResult, queryResult ->
                        if (!emitter.isDisposed) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                emitter.onSuccess(queryResult.productDetailsList)
                            } else {
                                emitter.tryOnError(BillingNotSupportedException(billingResult))
                            }
                        }
                    }
                } catch (error: Throwable) {
                    Exceptions.throwIfFatal(error)
                    emitter.tryOnError(error)
                }
            }
            .observeOn(backgroundScheduler)


    override fun getAllPurchases(productType: String): Single<List<Purchase>> =
        Single
            .create { emitter ->
                try {
                    billingClient
                        .queryPurchasesAsync(
                            QueryPurchasesParams
                                .newBuilder()
                                .setProductType(productType)
                                .build()
                        ) { billingResult, purchases ->
                            if (!emitter.isDisposed) {
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                    emitter.onSuccess(purchases)
                                } else {
                                    emitter.tryOnError(BillingNotSupportedException(billingResult))
                                }
                            }
                        }
                } catch (error: Throwable) {
                    Exceptions.throwIfFatal(error)
                    emitter.tryOnError(error)
                }
            }


    override fun consumePurchase(purchase: Purchase): Completable =
        Completable
            .create { emitter ->
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                try {
                    billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                        if (!emitter.isDisposed) {
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                emitter.onComplete()
                            } else {
                                emitter.tryOnError(BillingNotSupportedException(billingResult))
                            }
                        }
                    }
                } catch (error: Throwable) {
                    Exceptions.throwIfFatal(error)
                    emitter.tryOnError(error)
                }
            }
            .observeOn(backgroundScheduler)
}