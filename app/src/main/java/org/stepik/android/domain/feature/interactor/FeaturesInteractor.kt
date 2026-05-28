package org.stepik.android.domain.feature.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.repository.FeaturesRepository
import javax.inject.Inject

class FeaturesInteractor @Inject constructor(
    private val featuresRepository: FeaturesRepository
) {
    fun preloadFeatures(): Completable =
        featuresRepository
            .getFeatures()
            .ignoreElement()

    fun isAuthMarketingAgreementEnabled(): Single<Boolean> =
        featuresRepository
            .getFeatures()
            .map(::isFeatureEnabled)
            .onErrorReturnItem(false)

    fun isAuthMarketingAgreementEnabledCached(): Boolean =
        featuresRepository
            .getCachedFeatures()
            ?.let(::isFeatureEnabled)
            ?: false

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
        const val AUTH_MARKETING_AGREEMENT_NAME = "AuthMarketingAgreement"
    }

    private fun isFeatureEnabled(features: List<Feature>): Boolean =
        features
            .firstOrNull { it.name == AUTH_MARKETING_AGREEMENT_NAME }
            ?.parameters
            ?.isEnabled == true
}
