package org.stepik.android.domain.course_purchase.interactor

import io.reactivex.Single
import org.solovyev.android.checkout.ProductTypes
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import javax.inject.Inject

class CoursePurchaseInteractor
@Inject
constructor(
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository
) {
    fun checkPromoCodeValidity(courseId: Long, promoCodeName: String): Single<PromoCodeSku> =
        coursePaymentsRepository
            .checkDeeplinkPromoCodeValidity(courseId, promoCodeName)
            .flatMap {
                mobileTiersRepository
                    .calculateMobileTier(MobileTierCalculation(course = courseId, promo = promoCodeName), dataSourceType = DataSourceType.REMOTE)
                    .flatMapSingle { mobileTier ->
                        if (mobileTier.promoTier == null) {
                            Single.just(PromoCodeSku.EMPTY)
                        } else {
                            lightSkuRepository
                                .getLightInventory(ProductTypes.IN_APP, listOf(mobileTier.promoTier), dataSourceType = DataSourceType.REMOTE)
                                .map { lightSku -> PromoCodeSku(promoCodeName, lightSku.firstOrNull()) }
                        }
                    }
            }
            .onErrorReturnItem(PromoCodeSku.EMPTY)
}