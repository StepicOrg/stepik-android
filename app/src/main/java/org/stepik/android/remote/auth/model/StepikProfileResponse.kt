package org.stepik.android.remote.auth.model

import org.stepik.android.model.user.Profile
import  org.stepik.android.model.user.User

class StepikProfileResponse(
    private val users: List<User>? = null,
    private val profiles: List<Profile>? = null
) {
    fun getProfile(): Profile? =
        profiles?.firstOrNull()

    fun getUser(): User? =
        users?.firstOrNull()
}
