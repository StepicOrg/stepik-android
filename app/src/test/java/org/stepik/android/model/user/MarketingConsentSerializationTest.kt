package org.stepik.android.model.user

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.stepic.droid.testUtils.TestingGsonProvider

class MarketingConsentSerializationTest {
    private val gson = TestingGsonProvider.gson

    @Test
    fun `registration credentials serialize subscribed for marketing true and false`() {
        assertTrue(
            gson.toJsonObject(
                RegistrationCredentials("John", "Doe", "john@stepik.org", "password", true)
            ).get("subscribed_for_marketing").asBoolean
        )

        assertFalse(
            gson.toJsonObject(
                RegistrationCredentials("John", "Doe", "john@stepik.org", "password", false)
            ).get("subscribed_for_marketing").asBoolean
        )
    }

    @Test
    fun `registration credentials omit subscribed for marketing when null`() {
        val json = gson.toJsonObject(
            RegistrationCredentials("John", "Doe", "john@stepik.org", "password")
        )

        assertFalse(json.has("subscribed_for_marketing"))
    }

    @Test
    fun `profile serializes and deserializes subscribed for marketing`() {
        val profile = Profile(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            fullName = "John Doe",
            subscribedForMarketing = true
        )

        val json = gson.toJsonObject(profile)
        assertTrue(json.get("subscribed_for_marketing").asBoolean)

        val deserialized = gson.fromJson(json, Profile::class.java)
        assertEquals(true, deserialized.subscribedForMarketing)
    }

    @Test
    fun `profile supports false and missing subscribed for marketing`() {
        val falseProfile = gson.fromJson(
            """
            {
              "id": 1,
              "subscribed_for_marketing": false
            }
            """.trimIndent(),
            Profile::class.java
        )
        assertEquals(false, falseProfile.subscribedForMarketing)

        val missingProfile = gson.fromJson(
            """
            {
              "id": 2
            }
            """.trimIndent(),
            Profile::class.java
        )
        assertNull(missingProfile.subscribedForMarketing)
    }

    private fun com.google.gson.Gson.toJsonObject(value: Any): JsonObject =
        toJsonTree(value).asJsonObject
}
