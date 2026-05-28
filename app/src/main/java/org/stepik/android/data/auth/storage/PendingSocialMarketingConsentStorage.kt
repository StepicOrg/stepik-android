package org.stepik.android.data.auth.storage

import android.content.Context
import android.content.SharedPreferences
import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent

class PendingSocialMarketingConsentStorage(
    private val sharedPreferences: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    )

    fun set(pendingSocialMarketingConsent: PendingSocialMarketingConsent) {
        if (pendingSocialMarketingConsent == PendingSocialMarketingConsent.NONE) {
            clear()
            return
        }

        sharedPreferences
            .edit()
            .putString(KEY_PENDING_SOCIAL_MARKETING_CONSENT, pendingSocialMarketingConsent.name)
            .apply()
    }

    fun get(): PendingSocialMarketingConsent =
        sharedPreferences
            .getString(KEY_PENDING_SOCIAL_MARKETING_CONSENT, null)
            ?.let(::safeValueOf)
            ?: PendingSocialMarketingConsent.NONE

    fun clear() {
        sharedPreferences
            .edit()
            .remove(KEY_PENDING_SOCIAL_MARKETING_CONSENT)
            .apply()
    }

    private companion object {
        private const val PREFERENCES_NAME = "pending_social_marketing_consent_storage"
        private const val KEY_PENDING_SOCIAL_MARKETING_CONSENT = "pending_social_marketing_consent"
    }
}

private fun safeValueOf(value: String): PendingSocialMarketingConsent? =
    runCatching { PendingSocialMarketingConsent.valueOf(value) }.getOrNull()
