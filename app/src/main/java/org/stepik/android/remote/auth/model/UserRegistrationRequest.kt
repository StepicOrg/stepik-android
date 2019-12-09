package org.stepik.android.remote.auth.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.RegistrationCredentials

class UserRegistrationRequest(
    @SerializedName("user")
    val user: RegistrationCredentials
)