package org.stepik.android.domain.debug.storage

import android.content.Context
import org.stepik.android.domain.debug.model.AuthMarketingAgreementDebugOverride
import org.stepik.android.domain.debug.model.EndpointConfig
import javax.inject.Inject

interface BackendFeaturesDebugStorage {
    fun getAuthMarketingAgreementOverride(endpointConfig: EndpointConfig): AuthMarketingAgreementDebugOverride
    fun setAuthMarketingAgreementOverride(
        endpointConfig: EndpointConfig,
        override: AuthMarketingAgreementDebugOverride
    )
    fun getRubricatorOverrideCacheFile(endpointConfig: EndpointConfig): String?
    fun setRubricatorOverrideCacheFile(endpointConfig: EndpointConfig, cacheFile: String?)
    fun clearOverrides(endpointConfig: EndpointConfig)
}

class BackendFeaturesDebugStorageImpl
@Inject
constructor(
    context: Context
) : BackendFeaturesDebugStorage {
    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getAuthMarketingAgreementOverride(endpointConfig: EndpointConfig): AuthMarketingAgreementDebugOverride =
        sharedPreferences
            .getString(authOverrideKey(endpointConfig), null)
            ?.let(::safeAuthOverrideValueOf)
            ?: AuthMarketingAgreementDebugOverride.SERVER

    override fun setAuthMarketingAgreementOverride(
        endpointConfig: EndpointConfig,
        override: AuthMarketingAgreementDebugOverride
    ) {
        sharedPreferences
            .edit()
            .putString(authOverrideKey(endpointConfig), override.name)
            .apply()
    }

    override fun getRubricatorOverrideCacheFile(endpointConfig: EndpointConfig): String? =
        sharedPreferences
            .getString(rubricatorOverrideKey(endpointConfig), null)
            ?.takeIf(String::isNotBlank)

    override fun setRubricatorOverrideCacheFile(endpointConfig: EndpointConfig, cacheFile: String?) {
        sharedPreferences
            .edit()
            .apply {
                if (cacheFile.isNullOrBlank()) {
                    remove(rubricatorOverrideKey(endpointConfig))
                } else {
                    putString(rubricatorOverrideKey(endpointConfig), cacheFile)
                }
            }
            .apply()
    }

    override fun clearOverrides(endpointConfig: EndpointConfig) {
        sharedPreferences
            .edit()
            .remove(authOverrideKey(endpointConfig))
            .remove(rubricatorOverrideKey(endpointConfig))
            .apply()
    }

    private fun authOverrideKey(endpointConfig: EndpointConfig): String =
        "${endpointConfig.name}_auth_marketing_agreement_override"

    private fun rubricatorOverrideKey(endpointConfig: EndpointConfig): String =
        "${endpointConfig.name}_rubricator_cache_file_override"

    private companion object {
        const val PREFERENCES_NAME = "backend_features_debug_storage"
    }
}

private fun safeAuthOverrideValueOf(value: String): AuthMarketingAgreementDebugOverride? =
    runCatching { AuthMarketingAgreementDebugOverride.valueOf(value) }.getOrNull()
