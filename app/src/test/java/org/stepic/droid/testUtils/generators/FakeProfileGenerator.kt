package org.stepic.droid.testUtils.generators

import org.stepik.android.model.Profile


object FakeProfileGenerator {

    @JvmOverloads
    fun generate(id: Long = 0,
                 firstName: String = "John",
                 lastName: String = "Doe",
                 avatar: String? = null,
                 shortBio: String = "",
                 details: String = ""
    ): Profile {
        return Profile(id = id,
                avatar = avatar,
                fullName = "$firstName $lastName",
                shortBio = shortBio,
                details = details)
    }

}
