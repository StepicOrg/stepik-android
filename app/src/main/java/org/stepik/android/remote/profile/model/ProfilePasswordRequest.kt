package org.stepik.android.remote.profile.model

import com.google.gson.annotations.SerializedName

class ProfilePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)