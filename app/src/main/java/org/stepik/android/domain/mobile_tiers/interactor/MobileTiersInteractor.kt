package org.stepik.android.domain.mobile_tiers.interactor

import io.reactivex.Maybe
import org.solovyev.android.checkout.ProductTypes
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
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
}