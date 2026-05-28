package org.stepik.android.domain.auth.mapper

import org.stepik.android.domain.auth.model.RegistrationConsentState

sealed class RegistrationConsentResult {
    object Valid : RegistrationConsentResult()
    object RequiredConsentMissing : RegistrationConsentResult()
}

class RegistrationConsentMapper {

    fun validate(state: RegistrationConsentState): RegistrationConsentResult =
        if (state.isRequiredConsentGranted) {
            RegistrationConsentResult.Valid
        } else {
            RegistrationConsentResult.RequiredConsentMissing
        }

    fun mapSubscribedForMarketing(state: RegistrationConsentState): Boolean? =
        if (state.isMarketingVisible) state.isMarketingChecked else null
}
