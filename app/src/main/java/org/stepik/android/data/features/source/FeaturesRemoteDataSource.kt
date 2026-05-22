package org.stepik.android.data.features.source

import io.reactivex.Single
import org.stepik.android.domain.feature.model.Feature

interface FeaturesRemoteDataSource {
    fun getFeatures(): Single<List<Feature>>
}