package org.stepik.android.domain.debug.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.Purchase
import org.stepik.android.domain.billing.repository.BillingRepository
import timber.log.Timber
import javax.inject.Inject

class InAppPurchasesInteractor
@Inject
constructor(
    private val billingRepository: BillingRepository
) {
    fun getAllPurchases(): Single<List<Purchase>> =
        billingRepository.getAllPurchases(ProductTypes.IN_APP)

    fun consumePurchase(purchase: Purchase): Completable =
        billingRepository.consumePurchase(purchase)

    fun consumePurchases(purchases: List<Purchase>): Completable =
        purchases
            .toObservable()
            .flatMapCompletable { billingRepository.consumePurchase(it) }
}