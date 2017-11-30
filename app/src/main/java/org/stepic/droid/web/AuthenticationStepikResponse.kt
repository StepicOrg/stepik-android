package org.stepic.droid.web

import com.google.gson.annotations.SerializedName

class AuthenticationStepikResponse(
        @SerializedName("refresh_token")
        val refreshToken: String? = null,
        @SerializedName("expires_in")
        val expiresIn: Long = 0,
        @SerializedName("access_token")
        val accessToken: String? = null,
        @SerializedName("token_type")
        val tokenType: String? = null
) {
    fun isSuccess(): Boolean = refreshToken != null && accessToken != null && expiresIn > 0
}
