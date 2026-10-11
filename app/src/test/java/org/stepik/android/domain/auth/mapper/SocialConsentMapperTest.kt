package org.stepik.android.domain.auth.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent
import org.stepik.android.domain.auth.model.RegistrationConsentState
import org.stepik.android.view.auth.model.SocialNetwork

class SocialConsentMapperTest {

    private val mapper = SocialConsentMapper

    // AC1: Required Consent Blocks Provider Launch

    @Test
    fun `required consent unchecked returns RequiredConsentMissing`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE)
        assertTrue(result is SocialConsentResult.RequiredConsentMissing)
    }

    @Test
    fun `required consent unchecked blocks regardless of marketing state`() {
        val marketingChecked = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        assertTrue(mapper.mapConsent(marketingChecked, SocialNetwork.GOOGLE) is SocialConsentResult.RequiredConsentMissing)

        val marketingUnchecked = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        assertTrue(mapper.mapConsent(marketingUnchecked, SocialNetwork.VK) is SocialConsentResult.RequiredConsentMissing)
    }

    // AC2: Required Consent Allows Provider Launch

    @Test
    fun `required consent checked returns Valid with correct provider`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE)
        assertTrue(result is SocialConsentResult.Valid)
        assertEquals(SocialNetwork.GOOGLE, (result as SocialConsentResult.Valid).provider)
    }

    @Test
    fun `required consent checked allows with marketing visible and checked`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        val result = mapper.mapConsent(state, SocialNetwork.GITHUB)
        assertTrue(result is SocialConsentResult.Valid)
        assertEquals(SocialNetwork.GITHUB, (result as SocialConsentResult.Valid).provider)
    }

    // AC3: Marketing Hidden Clears Pending Consent

    @Test
    fun `marketing hidden maps pending consent to NONE`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE) as SocialConsentResult.Valid
        assertEquals(PendingSocialMarketingConsent.NONE, result.pendingConsent)
    }

    @Test
    fun `marketing hidden with checked checkbox still maps to NONE`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = true
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE) as SocialConsentResult.Valid
        assertEquals(PendingSocialMarketingConsent.NONE, result.pendingConsent)
    }

    // AC4: Marketing Checked Stores Subscribed

    @Test
    fun `marketing visible and checked maps to SUBSCRIBED`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE) as SocialConsentResult.Valid
        assertEquals(PendingSocialMarketingConsent.SUBSCRIBED, result.pendingConsent)
    }

    // AC5: Marketing Unchecked Stores Not Subscribed

    @Test
    fun `marketing visible and unchecked maps to NOT_SUBSCRIBED`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE) as SocialConsentResult.Valid
        assertEquals(PendingSocialMarketingConsent.NOT_SUBSCRIBED, result.pendingConsent)
    }

    // AC6: Selected Provider Is Preserved

    @Test
    fun `google provider is preserved in valid result`() {
        val state = validConsentState()
        val result = mapper.mapConsent(state, SocialNetwork.GOOGLE) as SocialConsentResult.Valid
        assertEquals(SocialNetwork.GOOGLE, result.provider)
    }

    @Test
    fun `vk provider is preserved in valid result`() {
        val state = validConsentState()
        val result = mapper.mapConsent(state, SocialNetwork.VK) as SocialConsentResult.Valid
        assertEquals(SocialNetwork.VK, result.provider)
    }

    @Test
    fun `github provider is preserved in valid result`() {
        val state = validConsentState()
        val result = mapper.mapConsent(state, SocialNetwork.GITHUB) as SocialConsentResult.Valid
        assertEquals(SocialNetwork.GITHUB, result.provider)
    }

    @Test
    fun `provider preserved regardless of checkbox state`() {
        val checkedState = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        val uncheckedState = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        val hiddenState = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        for (provider in SocialNetwork.values()) {
            assertEquals(provider, (mapper.mapConsent(checkedState, provider) as SocialConsentResult.Valid).provider)
            assertEquals(provider, (mapper.mapConsent(uncheckedState, provider) as SocialConsentResult.Valid).provider)
            assertEquals(provider, (mapper.mapConsent(hiddenState, provider) as SocialConsentResult.Valid).provider)
        }
    }

    // AC7: Social Screen Feature Snapshot Is Stable

    @Test
    fun `cached true shows marketing checked by default via snapshot`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true)
        assertTrue(state.isMarketingVisible)
        assertTrue(state.isMarketingChecked)
        assertFalse(state.isRequiredConsentGranted)
    }

    @Test
    fun `cached false hides marketing via snapshot`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false)
        assertFalse(state.isMarketingVisible)
        assertFalse(state.isMarketingChecked)
    }

    @Test
    fun `snapshot is immutable - later cache changes do not affect original state`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true)
        val updatedState = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false)
        assertTrue(state.isMarketingVisible)
        assertTrue(state.isMarketingChecked)
        assertFalse(updatedState.isMarketingVisible)
    }

    @Test
    fun `initial required consent is always unchecked via snapshot`() {
        assertFalse(RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true).isRequiredConsentGranted)
        assertFalse(RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false).isRequiredConsentGranted)
    }

    private fun validConsentState() = RegistrationConsentState(
        isRequiredConsentGranted = true,
        isMarketingVisible = false,
        isMarketingChecked = false
    )
}
