package org.stepik.android.remote.rating.model

import com.google.gson.annotations.SerializedName

class RatingRequest(
    @SerializedName("exp")
    val exp: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("token")
    val token: String?
)