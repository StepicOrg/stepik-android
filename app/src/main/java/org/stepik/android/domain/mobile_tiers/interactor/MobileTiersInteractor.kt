package org.stepik.android.domain.mobile_tiers.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.solovyev.android.checkout.ProductTypes
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class MobileTiersInteractor
@Inject
constructor(
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository
) {
    fun getMobileTier(courseId: Long): Maybe<MobileTier> =
        mobileTiersRepository.calculateMobileTier(MobileTierCalculation(course = courseId))

    fun getLightSku(skuId: String): Maybe<LightSku> =
        lightSkuRepository.getLightInventory(ProductTypes.IN_APP, listOf(skuId)).maybeFirst()

    fun fetchTiersAndSkus(courses: List<Course>, sourceType: DataSourceType): Single<Pair<List<MobileTier>, List<LightSku>>> {
        val mobileTierCalculations = courses.map { MobileTierCalculation(course = it.id) }
        return mobileTiersRepository
            .calculateMobileTiers(mobileTierCalculations)
            .flatMap { mobileTiers ->
                val priceTiers = mobileTiers.mapNotNull(MobileTier::priceTier)
                val promoTiers = mobileTiers.mapNotNull(MobileTier::promoTier)
                val skuIds = priceTiers.union(promoTiers).toList()
                lightSkuRepository
                    .getLightInventory(ProductTypes.IN_APP, skuIds, sourceType)
                    .map { lightSkus -> mobileTiers to lightSkus }
            }
            .onErrorReturnItem(emptyList<MobileTier>() to emptyList<LightSku>())
    }
}