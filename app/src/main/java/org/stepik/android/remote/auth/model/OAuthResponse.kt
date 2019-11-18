package org.stepik.android.remote.auth.model

import com.google.gson.annotations.SerializedName

class OAuthResponse(
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)