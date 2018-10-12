package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class UserCourse(
        val id: Long,
        val user: Long,
        val course: Long,
        @SerializedName("is_favorite")
        val isFavorite: Boolean,
        @SerializedName("last_viewed")
        val lastViewed: String
)