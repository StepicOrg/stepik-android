package org.stepik.android.data.mobile_tiers.source

import io.reactivex.Single
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

interface MobileTiersRemoteDataSource {
    fun getMobileTiers(mobileTierCalculations: List<MobileTierCalculation>): Single<List<MobileTier>>
}