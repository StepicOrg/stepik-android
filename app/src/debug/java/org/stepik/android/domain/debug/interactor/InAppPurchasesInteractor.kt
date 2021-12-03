package org.stepik.android.domain.debug.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.domain.billing.repository.BillingRepository
import javax.inject.Inject

class InAppPurchasesInteractor
@Inject
constructor(
    private val billingRepository: BillingRepository
) {
    fun getAllPurchases(): Single<List<Purchase>> =
        billingRepository.getAllPurchases(BillingClient.SkuType.INAPP)

    fun consumePurchase(purchase: Purchase): Completable =
        billingRepository.consumePurchase(purchase)

    fun consumePurchases(purchases: List<Purchase>): Completable =
        purchases
            .toObservable()
            .flatMapCompletable { billingRepository.consumePurchase(it) }
}