package org.stepic.droid.web

import org.stepic.droid.model.Profile
import org.stepic.droid.model.User

data class StepicProfileResponse(private val users: List<User>? = null,
                                 private val profiles: List<Profile>? = null) {

    fun getProfile(): Profile? = profiles?.firstOrNull()
    fun getUser(): User? = users?.firstOrNull()
}
