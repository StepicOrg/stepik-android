package org.stepik.android.domain.feature.interactor

import io.reactivex.Single
import org.stepik.android.domain.feature.repository.FeaturesRepository
import javax.inject.Inject

class FeaturesInteractor @Inject constructor(
    private val featuresRepository: FeaturesRepository
) {
    fun fetchRubricatorUrl(): Single<String> =
        featuresRepository
            .getFeatures()
            .map { features ->
                features
                    .firstOrNull { it.name == RUBRICATOR_NAME }
                    ?.parameters
                    ?.cacheFile
                    ?: ""
            }

    private companion object {
        const val RUBRICATOR_NAME = "Rubricator"
    }
}