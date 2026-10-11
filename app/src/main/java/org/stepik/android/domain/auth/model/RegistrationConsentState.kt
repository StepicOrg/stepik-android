package org.stepik.android.domain.auth.model

data class RegistrationConsentState(
    val isRequiredConsentGranted: Boolean,
    val isMarketingVisible: Boolean,
    val isMarketingChecked: Boolean
) {
    companion object {
        fun fromFeatureSnapshot(isMarketingEnabled: Boolean): RegistrationConsentState =
            RegistrationConsentState(
                isRequiredConsentGranted = false,
                isMarketingVisible = isMarketingEnabled,
                isMarketingChecked = isMarketingEnabled
            )
    }
}
