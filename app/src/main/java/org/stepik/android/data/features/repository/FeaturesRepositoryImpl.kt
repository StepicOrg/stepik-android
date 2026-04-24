package org.stepik.android.data.features.repository

import io.reactivex.Single
import org.stepik.android.data.features.source.FeaturesRemoteDataSource
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.repository.FeaturesRepository
import javax.inject.Inject

class FeaturesRepositoryImpl @Inject constructor(
    private val featuresRemoteDataSource: FeaturesRemoteDataSource
) : FeaturesRepository {
    override fun getFeatures(): Single<List<Feature>> =
        featuresRemoteDataSource.getFeatures()
}