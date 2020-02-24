package org.stepik.android.domain.auth.model

import com.google.gson.annotations.SerializedName

class RegistrationError(
    @SerializedName("email")
    val email: List<String>?,
    @SerializedName("password")
    val password: List<String?>,

    @SerializedName("first_name")
    val firstName: List<String?>?,
    @SerializedName("last_name")
    val lastName: List<String?>?
)