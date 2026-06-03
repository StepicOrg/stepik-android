package org.stepik.android.domain.auth.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.stepic.droid.util.ValidatorUtil
import org.stepik.android.domain.auth.model.RegistrationConsentState
import org.stepik.android.model.user.RegistrationCredentials

class RegistrationConsentMapperTest {

    private val mapper = RegistrationConsentMapper

    // AC1: Required Consent Blocks Registration

    @Test
    fun `required consent unchecked returns RequiredConsentMissing`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val result = mapper.validate(state)
        assertTrue(result is RegistrationConsentResult.RequiredConsentMissing)
    }

    @Test
    fun `required consent unchecked blocks regardless of marketing state`() {
        val marketingChecked = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        assertTrue(mapper.validate(marketingChecked) is RegistrationConsentResult.RequiredConsentMissing)

        val marketingUnchecked = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        assertTrue(mapper.validate(marketingUnchecked) is RegistrationConsentResult.RequiredConsentMissing)
    }

    // AC2: Required Consent Allows Registration

    @Test
    fun `required consent checked returns Valid`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val result = mapper.validate(state)
        assertTrue(result is RegistrationConsentResult.Valid)
    }

    @Test
    fun `required consent checked allows submission with marketing visible`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        assertTrue(mapper.validate(state) is RegistrationConsentResult.Valid)
    }

    // AC3: Marketing Payload Mapping

    @Test
    fun `marketing hidden maps subscribedForMarketing to null`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        assertNull(mapper.mapSubscribedForMarketing(state))
    }

    @Test
    fun `marketing visible and checked maps subscribedForMarketing to true`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        assertEquals(true, mapper.mapSubscribedForMarketing(state))
    }

    @Test
    fun `marketing visible and unchecked maps subscribedForMarketing to false`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        assertEquals(false, mapper.mapSubscribedForMarketing(state))
    }

    // AC3 extended: credentials carry correct marketing value

    @Test
    fun `credentials have null subscribedForMarketing when marketing hidden`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        val credentials = RegistrationCredentials(
            firstName = "Test",
            lastName = " ",
            email = "test@example.com",
            password = "123456",
            subscribedForMarketing = mapper.mapSubscribedForMarketing(state)
        )
        assertNull(credentials.subscribedForMarketing)
    }

    @Test
    fun `credentials have true subscribedForMarketing when marketing visible and checked`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = true
        )
        val credentials = RegistrationCredentials(
            firstName = "Test",
            lastName = " ",
            email = "test@example.com",
            password = "123456",
            subscribedForMarketing = mapper.mapSubscribedForMarketing(state)
        )
        assertEquals(true, credentials.subscribedForMarketing)
    }

    @Test
    fun `credentials have false subscribedForMarketing when marketing visible and unchecked`() {
        val state = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = true,
            isMarketingChecked = false
        )
        val credentials = RegistrationCredentials(
            firstName = "Test",
            lastName = " ",
            email = "test@example.com",
            password = "123456",
            subscribedForMarketing = mapper.mapSubscribedForMarketing(state)
        )
        assertEquals(false, credentials.subscribedForMarketing)
    }

    // AC4: Initial Feature Snapshot Is Stable

    @Test
    fun `cached true shows marketing visible and checked by default`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true)
        assertTrue(state.isMarketingVisible)
        assertTrue(state.isMarketingChecked)
        assertFalse(state.isRequiredConsentGranted)
    }

    @Test
    fun `cached false hides marketing`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false)
        assertFalse(state.isMarketingVisible)
        assertFalse(state.isMarketingChecked)
    }

    @Test
    fun `initial state is immutable - later cache changes do not affect original state`() {
        val state = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true)
        // Simulate the cache changing after activity creation
        val updatedState = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false)
        // Original state is unaffected
        assertTrue(state.isMarketingVisible)
        assertTrue(state.isMarketingChecked)
        // Updated state reflects the new snapshot
        assertFalse(updatedState.isMarketingVisible)
    }

    @Test
    fun `initial required consent is always unchecked`() {
        val enabledState = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = true)
        assertFalse(enabledState.isRequiredConsentGranted)

        val disabledState = RegistrationConsentState.fromFeatureSnapshot(isMarketingEnabled = false)
        assertFalse(disabledState.isRequiredConsentGranted)
    }

    // AC5: Existing Registration Validation Is Preserved

    @Test
    fun `existing password validation still blocks short passwords`() {
        assertFalse(ValidatorUtil.isPasswordValid(""))
        assertFalse(ValidatorUtil.isPasswordValid("12345"))
    }

    @Test
    fun `existing password validation allows valid passwords`() {
        assertTrue(ValidatorUtil.isPasswordValid("123456"))
    }

    @Test
    fun `consent validation and field validation are independent - consent allows but password blocks`() {
        val validConsent = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        assertTrue(mapper.validate(validConsent) is RegistrationConsentResult.Valid)
        // Field validation still blocks
        assertFalse(ValidatorUtil.isPasswordValid(""))
    }

    @Test
    fun `consent validation and field validation are independent - password allows but consent blocks`() {
        val invalidConsent = RegistrationConsentState(
            isRequiredConsentGranted = false,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        assertTrue(mapper.validate(invalidConsent) is RegistrationConsentResult.RequiredConsentMissing)
        // Field validation allows
        assertTrue(ValidatorUtil.isPasswordValid("123456"))
    }

    @Test
    fun `both validations pass with valid fields and valid consent`() {
        val validConsent = RegistrationConsentState(
            isRequiredConsentGranted = true,
            isMarketingVisible = false,
            isMarketingChecked = false
        )
        assertTrue(mapper.validate(validConsent) is RegistrationConsentResult.Valid)
        assertTrue(ValidatorUtil.isPasswordValid("123456"))
    }
}
