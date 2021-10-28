package org.stepik.android.remote.mobile_tiers

import io.reactivex.Single
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.remote.mobile_tiers.model.MobileTiersResponse
import org.stepik.android.remote.mobile_tiers.service.MobileTiersService
import javax.inject.Inject

class MobileTiersRemoteDataSourceImpl
@Inject
constructor(
    private val mobileTiersService: MobileTiersService
) : MobileTiersRemoteDataSource {
    override fun getMobileTiers(mobileTierCalculations: List<MobileTierCalculation>): Single<List<MobileTier>> =
        mobileTiersService
            .calculateMobileTiers(mobileTierCalculations)
            .map(MobileTiersResponse::mobileTiers)
}