package org.stepik.android.remote.wishlist.model

import com.google.gson.annotations.SerializedName

class WishlistRequest(
    @SerializedName("wish-list")
    val body: Body
) {
    constructor(courseId: Long) : this(Body(courseId))

    class Body(
        @SerializedName("course")
        val course: Long,
        @SerializedName("platform")
        val platform: String = "mobile"
    )
}