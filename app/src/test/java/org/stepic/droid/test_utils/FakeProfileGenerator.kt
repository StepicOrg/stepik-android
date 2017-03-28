package org.stepic.droid.test_utils

import org.stepic.droid.model.Profile

object FakeProfileGenerator {

    @JvmOverloads
    fun generateFakeProfile(id: Long = 0,
                            firstName: String = "John",
                            lastName: String = "Doe",
                            avatar: String? = null,
                            shortBio: String = "",
                            details: String = ""
    ): Profile {
        return Profile(id = id,
                avatar = avatar,
                first_name = firstName,
                last_name = lastName,
                short_bio = shortBio,
                details = details)
    }
}
