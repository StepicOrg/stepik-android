package org.stepik.android.remote.features

import io.reactivex.Single
import org.stepik.android.data.features.source.FeaturesRemoteDataSource
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.remote.features.model.FeaturesResponse
import org.stepik.android.remote.features.service.FeaturesService
import javax.inject.Inject

class FeaturesRemoteDataSourceImpl @Inject constructor(
    private val featuresService: FeaturesService
) : FeaturesRemoteDataSource {
    override fun getFeatures(): Single<List<Feature>> =
        featuresService
            .getFeatures()
            .map(FeaturesResponse::features)
}