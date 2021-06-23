package org.stepik.android.remote.wishlist.model

import com.google.gson.annotations.SerializedName

data class WishlistWrapper(
    @SerializedName("courses")
    val courses: List<Long>?
) {
    companion object {
        val EMPTY = WishlistWrapper(courses = emptyList())
    }
}