package org.stepik.android.domain.debug.model

import org.stepik.android.domain.feature.model.Feature

data class BackendFeaturesDebugData(
    val endpointConfig: EndpointConfig,
    val serverFeatures: List<Feature>,
    val serverAuthMarketingAgreement: Boolean?,
    val activeAuthMarketingAgreement: Boolean,
    val authMarketingAgreementOverride: AuthMarketingAgreementDebugOverride,
    val serverRubricatorCacheFile: String?,
    val activeRubricatorCacheFile: String?,
    val rubricatorOverrideCacheFile: String?,
    val featuresError: String? = null
)
