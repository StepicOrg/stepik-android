package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class SocialProfile(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)