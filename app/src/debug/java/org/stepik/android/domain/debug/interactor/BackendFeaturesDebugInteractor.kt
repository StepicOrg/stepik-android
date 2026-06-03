package org.stepik.android.domain.debug.interactor

import io.reactivex.Single
import org.stepik.android.domain.debug.endpoint.BackendFeaturesDebugEndpointProvider
import org.stepik.android.domain.debug.model.AuthMarketingAgreementDebugOverride
import org.stepik.android.domain.debug.model.BackendFeaturesDebugData
import org.stepik.android.domain.debug.model.EndpointConfig
import org.stepik.android.domain.debug.storage.BackendFeaturesDebugStorage
import org.stepik.android.domain.feature.model.Feature
import org.stepik.android.domain.feature.model.Parameters
import org.stepik.android.domain.feature.repository.FeaturesRepository
import javax.inject.Inject

class BackendFeaturesDebugInteractor
@Inject
constructor(
    private val featuresRepository: FeaturesRepository,
    private val backendFeaturesDebugEndpointProvider: BackendFeaturesDebugEndpointProvider,
    private val backendFeaturesDebugStorage: BackendFeaturesDebugStorage
) {
    fun getDebugData(forceUpdate: Boolean): Single<BackendFeaturesDebugData> =
        featuresRepository
            .getFeatures(forceUpdate)
            .map(::buildAndApplyDebugData)
            .onErrorReturn { error ->
                buildDebugData(
                    serverFeatures = featuresRepository.getCachedFeatures().orEmpty(),
                    featuresError = error.message ?: error.javaClass.simpleName
                )
            }

    fun setAuthMarketingAgreementOverride(
        override: AuthMarketingAgreementDebugOverride,
        serverFeatures: List<Feature>
    ): BackendFeaturesDebugData {
        backendFeaturesDebugStorage.setAuthMarketingAgreementOverride(getEndpointConfig(), override)
        return buildAndApplyDebugData(serverFeatures)
    }

    fun setRubricatorOverrideCacheFile(
        cacheFile: String?,
        serverFeatures: List<Feature>
    ): BackendFeaturesDebugData {
        backendFeaturesDebugStorage.setRubricatorOverrideCacheFile(getEndpointConfig(), cacheFile)
        return buildAndApplyDebugData(serverFeatures)
    }

    fun clearOverrides(serverFeatures: List<Feature>): BackendFeaturesDebugData {
        backendFeaturesDebugStorage.clearOverrides(getEndpointConfig())
        return buildAndApplyDebugData(serverFeatures)
    }

    private fun buildAndApplyDebugData(serverFeatures: List<Feature>): BackendFeaturesDebugData {
        val data = buildDebugData(serverFeatures)
        featuresRepository.replaceCachedFeatures(applyOverrides(serverFeatures, data))
        return data
    }

    private fun buildDebugData(
        serverFeatures: List<Feature>,
        featuresError: String? = null
    ): BackendFeaturesDebugData {
        val endpointConfig = getEndpointConfig()
        val authOverride = backendFeaturesDebugStorage.getAuthMarketingAgreementOverride(endpointConfig)
        val rubricatorOverrideCacheFile = backendFeaturesDebugStorage.getRubricatorOverrideCacheFile(endpointConfig)
        val activeFeatures = applyOverrides(
            serverFeatures,
            authOverride,
            rubricatorOverrideCacheFile
        )

        return BackendFeaturesDebugData(
            endpointConfig = endpointConfig,
            serverFeatures = serverFeatures,
            serverAuthMarketingAgreement = getAuthMarketingAgreementValue(serverFeatures),
            activeAuthMarketingAgreement = getAuthMarketingAgreementValue(activeFeatures) == true,
            authMarketingAgreementOverride = authOverride,
            serverRubricatorCacheFile = getRubricatorCacheFile(serverFeatures),
            activeRubricatorCacheFile = getRubricatorCacheFile(activeFeatures),
            rubricatorOverrideCacheFile = rubricatorOverrideCacheFile,
            featuresError = featuresError
        )
    }

    private fun applyOverrides(serverFeatures: List<Feature>, data: BackendFeaturesDebugData): List<Feature> =
        applyOverrides(
            serverFeatures,
            data.authMarketingAgreementOverride,
            data.rubricatorOverrideCacheFile
        )

    private fun applyOverrides(
        serverFeatures: List<Feature>,
        authOverride: AuthMarketingAgreementDebugOverride,
        rubricatorOverrideCacheFile: String?
    ): List<Feature> {
        var features = serverFeatures
        features = when (authOverride) {
            AuthMarketingAgreementDebugOverride.SERVER ->
                features
            AuthMarketingAgreementDebugOverride.FORCE_ENABLED ->
                upsertFeature(features, AUTH_MARKETING_AGREEMENT_NAME) {
                    it.copy(isEnabled = true)
                }
            AuthMarketingAgreementDebugOverride.FORCE_DISABLED ->
                upsertFeature(features, AUTH_MARKETING_AGREEMENT_NAME) {
                    it.copy(isEnabled = false)
                }
        }

        if (!rubricatorOverrideCacheFile.isNullOrBlank()) {
            features = upsertFeature(features, RUBRICATOR_NAME) {
                it.copy(cacheFile = rubricatorOverrideCacheFile)
            }
        }

        return features
    }

    private fun upsertFeature(
        features: List<Feature>,
        name: String,
        updateParameters: (Parameters) -> Parameters
    ): List<Feature> {
        val index = features.indexOfFirst { it.name == name }
        if (index == -1) {
            return features + Feature(
                id = DEBUG_FEATURE_ID,
                name = name,
                parameters = updateParameters(Parameters())
            )
        }

        return features.mapIndexed { featureIndex, feature ->
            if (featureIndex == index) {
                feature.copy(parameters = updateParameters(feature.parameters))
            } else {
                feature
            }
        }
    }

    private fun getAuthMarketingAgreementValue(features: List<Feature>): Boolean? =
        features
            .firstOrNull { it.name == AUTH_MARKETING_AGREEMENT_NAME }
            ?.parameters
            ?.isEnabled

    private fun getRubricatorCacheFile(features: List<Feature>): String? =
        features
            .firstOrNull { it.name == RUBRICATOR_NAME }
            ?.parameters
            ?.cacheFile

    private fun getEndpointConfig(): EndpointConfig =
        backendFeaturesDebugEndpointProvider.getEndpointConfig()

    private companion object {
        const val AUTH_MARKETING_AGREEMENT_NAME = "AuthMarketingAgreement"
        const val RUBRICATOR_NAME = "Rubricator"
        const val DEBUG_FEATURE_ID = 0L
    }
}
