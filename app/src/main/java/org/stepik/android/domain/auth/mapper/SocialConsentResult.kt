package org.stepik.android.domain.auth.mapper

import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent
import org.stepik.android.view.auth.model.SocialNetwork

sealed class SocialConsentResult {
    object RequiredConsentMissing : SocialConsentResult()
    data class Valid(
        val provider: SocialNetwork,
        val pendingConsent: PendingSocialMarketingConsent
    ) : SocialConsentResult()
}
