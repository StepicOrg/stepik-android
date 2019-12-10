package org.stepik.android.remote.auth.model

import com.google.gson.annotations.SerializedName

class SocialAuthError(
    @SerializedName("error")
    val error: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("provider")
    val provider: String?
)