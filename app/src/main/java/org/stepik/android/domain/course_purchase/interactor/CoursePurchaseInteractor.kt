package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import javax.inject.Inject

class CoursePurchaseInteractor
@Inject
constructor(
    private val billingRepository: BillingRepository,
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository
) {
    fun checkPromoCodeValidity(courseId: Long, promoCodeName: String): Single<PromoCodeSku> =
        mobileTiersRepository
            .calculateMobileTier(MobileTierCalculation(course = courseId, promo = promoCodeName), dataSourceType = DataSourceType.REMOTE)
            .flatMapSingle { mobileTier ->
                if (mobileTier.promoTier == null) {
                    Single.just(PromoCodeSku.EMPTY)
                } else {
                    lightSkuRepository
                        .getLightInventory(BillingClient.SkuType.INAPP, listOf(mobileTier.promoTier), dataSourceType = DataSourceType.REMOTE)
                        .map { lightSku -> PromoCodeSku(promoCodeName, lightSku.firstOrNull()) }
                }
            }

    fun getSkuDetails(skuId: String): Maybe<SkuDetails> =
        billingRepository.getInventory(BillingClient.SkuType.INAPP, skuId)
}