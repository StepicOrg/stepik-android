package org.stepik.android.domain.mobile_tiers.interactor

import com.android.billingclient.api.BillingClient
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import javax.inject.Inject

class MobileTiersInteractor
@Inject
constructor(
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository
) {
    fun fetchTiersAndSkus(courses: List<Course>, sourceType: DataSourceType): Single<Pair<List<MobileTier>, List<LightSku>>> {
        val mobileTierCalculations = courses
                .filter(Course::isPaid)
                .map { MobileTierCalculation(course = it.id) }
        return mobileTiersRepository
            .calculateMobileTiers(mobileTierCalculations)
            .flatMap { mobileTiers ->
                val priceTiers = mobileTiers.mapNotNull(MobileTier::priceTier)
                val promoTiers = mobileTiers.mapNotNull(MobileTier::promoTier)
                val skuIds = priceTiers.union(promoTiers).toList()
                lightSkuRepository
                    .getLightInventory(BillingClient.SkuType.INAPP, skuIds, sourceType)
                    .map { lightSkus -> mobileTiers to lightSkus }
            }
            .onErrorReturnItem(emptyList<MobileTier>() to emptyList<LightSku>())
    }
}