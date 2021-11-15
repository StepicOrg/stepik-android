package org.stepik.android.remote.mobile_tiers

import io.reactivex.Single
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.remote.mobile_tiers.service.MobileTiersService
import java.util.Locale
import javax.inject.Inject

class MobileTiersRemoteDataSourceImpl
@Inject
constructor(
    private val mobileTiersService: MobileTiersService
) : MobileTiersRemoteDataSource {
    override fun getMobileTiers(mobileTierCalculations: List<MobileTierCalculation>): Single<List<MobileTier>> =
        mobileTiersService
            .calculateMobileTiers(mobileTierCalculations)
            .map { response ->
                response.mobileTiers.map {
                    it.copy(
                        priceTier = "price_${it.priceTier.toLowerCase(Locale.ROOT).replace(' ', '_')}",
                        promoTier = it.promoTier?.let { promo -> "price_${promo.toLowerCase(Locale.ROOT).replace(' ', '_')}" }
                    )
                }
            }
}