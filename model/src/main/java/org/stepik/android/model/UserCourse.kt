package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class UserCourse(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("user")
    val user: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("is_favorite")
    val isFavorite: Boolean,
    @SerializedName("is_pinned")
    val isPinned: Boolean,
    @SerializedName("is_archived")
    val isArchived: Boolean,
    @SerializedName("last_viewed")
    val lastViewed: String
)