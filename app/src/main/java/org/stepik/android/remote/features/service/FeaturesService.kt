package org.stepik.android.remote.features.service

import io.reactivex.Single
import org.stepik.android.remote.features.model.FeaturesResponse
import retrofit2.http.GET

interface FeaturesService {
    @GET("api/features")
    fun getFeatures(): Single<FeaturesResponse>
}