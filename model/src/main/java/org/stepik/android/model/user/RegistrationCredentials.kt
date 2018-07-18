package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName

data class RegistrationCredentials(
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("last_name")
        val lastName: String,

        val email: String,
        val password: String
)