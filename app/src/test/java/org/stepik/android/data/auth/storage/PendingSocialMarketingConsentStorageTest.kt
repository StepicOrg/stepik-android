package org.stepik.android.data.auth.storage

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent

@RunWith(RobolectricTestRunner::class)
class PendingSocialMarketingConsentStorageTest {
    private lateinit var storage: PendingSocialMarketingConsentStorage
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        storage = PendingSocialMarketingConsentStorage(context)
    }

    @Test
    fun `returns none when preference is absent`() {
        assertEquals(PendingSocialMarketingConsent.NONE, storage.get())
    }

    @Test
    fun `stores and reads subscribed and not subscribed values`() {
        storage.set(PendingSocialMarketingConsent.SUBSCRIBED)
        assertEquals(PendingSocialMarketingConsent.SUBSCRIBED, storage.get())

        storage.set(PendingSocialMarketingConsent.NOT_SUBSCRIBED)
        assertEquals(PendingSocialMarketingConsent.NOT_SUBSCRIBED, storage.get())
    }

    @Test
    fun `clear removes stored value`() {
        storage.set(PendingSocialMarketingConsent.SUBSCRIBED)

        storage.clear()

        assertEquals(PendingSocialMarketingConsent.NONE, storage.get())
    }

    @Test
    fun `setting none clears stored value`() {
        storage.set(PendingSocialMarketingConsent.SUBSCRIBED)

        storage.set(PendingSocialMarketingConsent.NONE)

        assertEquals(PendingSocialMarketingConsent.NONE, storage.get())
    }

    @Test
    fun `invalid stored value maps to none`() {
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_SOCIAL_MARKETING_CONSENT, "invalid")
            .commit()

        assertEquals(PendingSocialMarketingConsent.NONE, storage.get())
    }

    private companion object {
        private const val PREFERENCES_NAME = "pending_social_marketing_consent_storage"
        private const val KEY_PENDING_SOCIAL_MARKETING_CONSENT = "pending_social_marketing_consent"
    }
}
