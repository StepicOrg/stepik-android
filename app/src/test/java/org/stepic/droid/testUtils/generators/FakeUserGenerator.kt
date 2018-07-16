package org.stepic.droid.testUtils.generators

import org.stepik.android.model.user.User

object FakeUserGenerator {

    @JvmOverloads
    fun generate(id: Long = 0,
                 firstName: String = "John",
                 lastName: String = "Doe",
                 avatar: String? = null,
                 shortBio: String = "",
                 details: String = ""
    ): User {
        return User(id = id,
                avatar = avatar,
                fullName = "$firstName $lastName",
                shortBio = shortBio,
                details = details,
                joinDate = null)
    }
}
