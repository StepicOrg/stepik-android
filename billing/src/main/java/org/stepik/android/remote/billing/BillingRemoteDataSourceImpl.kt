package org.stepik.android.remote.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
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

    override fun getInventory(productType: String, skuIds: List<String>): Single<List<SkuDetails>> =
        Single
            .create<List<SkuDetails>> { emitter ->
                val skuParams = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuIds)
                    .setType(productType)
                    .build()

                billingClient.querySkuDetailsAsync(skuParams) { billingResult, skuDetails ->
                    if (!emitter.isDisposed) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null) {
                            emitter.onSuccess(skuDetails)
                        } else {
                            emitter.onError(Exception(billingResult.debugMessage))
                        }
                    }
                }
            }
            .observeOn(backgroundScheduler)


    override fun getAllPurchases(productType: String): Single<List<Purchase>> =
        Single
            .create { emitter ->
                billingClient
                    .queryPurchasesAsync(productType) { billingResult, purchases ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            emitter.onSuccess(purchases)
                        } else {
                            emitter.onError(Exception(billingResult.debugMessage))
                        }
                    }
            }


    override fun consumePurchase(purchase: Purchase): Completable =
        Completable
            .create { emitter ->
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                    if (!emitter.isDisposed) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            emitter.onComplete()
                        } else {
                            emitter.onError(BillingNotSupportedException())
                        }
                    }
                }
            }
            .observeOn(backgroundScheduler)
}