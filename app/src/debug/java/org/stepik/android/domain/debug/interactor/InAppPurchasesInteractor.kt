package org.stepik.android.domain.debug.interactor

import io.reactivex.Single
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.Purchase
import org.stepik.android.domain.billing.repository.BillingRepository
import javax.inject.Inject

class InAppPurchasesInteractor
@Inject
constructor(
    private val billingRepository: BillingRepository
) {
    fun getAllPurchases(): Single<List<Purchase>> =
        billingRepository.getAllPurchases(ProductTypes.IN_APP)
}