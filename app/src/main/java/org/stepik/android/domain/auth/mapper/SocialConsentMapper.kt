package org.stepik.android.domain.auth.mapper

import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent
import org.stepik.android.domain.auth.model.RegistrationConsentState
import org.stepik.android.view.auth.model.SocialNetwork

object SocialConsentMapper {

    fun mapConsent(state: RegistrationConsentState, provider: SocialNetwork): SocialConsentResult =
        if (state.isRequiredConsentGranted) {
            SocialConsentResult.Valid(provider, mapMarketingConsent(state))
        } else {
            SocialConsentResult.RequiredConsentMissing
        }

    private fun mapMarketingConsent(state: RegistrationConsentState): PendingSocialMarketingConsent =
        if (!state.isMarketingVisible) {
            PendingSocialMarketingConsent.NONE
        } else if (state.isMarketingChecked) {
            PendingSocialMarketingConsent.SUBSCRIBED
        } else {
            PendingSocialMarketingConsent.NOT_SUBSCRIBED
        }
}
