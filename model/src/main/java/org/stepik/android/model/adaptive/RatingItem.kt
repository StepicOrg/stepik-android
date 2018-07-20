package org.stepik.android.model.adaptive

import com.google.gson.annotations.SerializedName

data class RatingItem(
        val rank: Int,
        val name: String?,
        val exp: Long,
        val user: Long,
        @SerializedName("is_not_fake")
        val isNotFake: Boolean = false
)