package org.stepik.android.domain.feature.repository

import io.reactivex.Single
import org.stepik.android.domain.feature.model.Feature

interface FeaturesRepository {
    fun getFeatures(): Single<List<Feature>>
}