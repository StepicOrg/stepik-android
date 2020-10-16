package org.stepik.android.domain.review.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("id")
    val id: Long,
    @SerializedName("session")
    val session: Long
)