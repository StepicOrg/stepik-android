package org.stepik.android.remote.mobile_tiers.service

import io.reactivex.Single
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.remote.mobile_tiers.model.MobileTiersResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MobileTiersService {
    @POST("api/mobile-tiers/calculate")
    fun calculateMobileTiers(@Body mobileTierCalculations: List<MobileTierCalculation>): Single<MobileTiersResponse>
}