package org.stepik.android.remote.auth.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.Profile
import  org.stepik.android.model.user.User

class StepikProfileResponse(
    @SerializedName("users")
    private val users: List<User>? = null,
    @SerializedName("profiles")
    private val profiles: List<Profile>? = null
) {
    fun getProfile(): Profile? =
        profiles?.firstOrNull()

    fun getUser(): User? =
        users?.firstOrNull()
}
