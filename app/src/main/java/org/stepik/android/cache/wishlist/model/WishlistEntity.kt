package org.stepik.android.cache.wishlist.model

import com.google.gson.annotations.SerializedName

data class WishlistEntity(
    @SerializedName("recordId")
    val recordId: Long,
    @SerializedName("courses")
    val courses: List<Long>
)
