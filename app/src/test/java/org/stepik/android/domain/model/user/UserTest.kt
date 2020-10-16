package org.stepik.android.domain.model.user

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.model.user.User
import java.util.*

@RunWith(RobolectricTestRunner::class)
class UserTest {
    companion object {
        fun createUser(): User =
            User(
                id = 123,
                profile = 9423,
                firstName = "Test",
                lastName = "Testov",
                fullName = "Test Testov",
                shortBio = "",
                details = "",
                avatar = "",
                cover = "",
                isPrivate = false,
                isGuest = false,
                isOrganization = false,
                socialProfiles = listOf(123, 4123, 12),
                knowledge = 123,
                knowledgeRank = 432,
                reputation = 456,
                reputationRank = 4567,
                createdCoursesCount = 42,
                followersCount = 1000,
                issuedCertificatesCount = 200,
                joinDate = Date()
            )
    }

    @Test
    fun userIsSerializable() {
        createUser()
            .assertThatObjectParcelable<User>()
    }
}