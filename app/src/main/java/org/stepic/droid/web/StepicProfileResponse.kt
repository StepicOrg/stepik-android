package org.stepic.droid.web

import org.stepik.android.model.user.Profile
import  org.stepik.android.model.user.User

class StepicProfileResponse(
        private val users: List<User>? = null,
        private val profiles: List<Profile>? = null
) {
    fun getProfile(): Profile? = profiles?.firstOrNull()
    fun getUser(): User? = users?.firstOrNull()
}
