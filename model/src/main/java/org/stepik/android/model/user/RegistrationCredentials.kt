package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName

class RegistrationCredentials(
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("last_name")
        val lastName: String,

        @SerializedName("email")
        val email: String,
        @SerializedName("password")
        val password: String
)