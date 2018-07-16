package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
        val id: Long = 0,
        val profile: Long = 0,

        @SerializedName("first_name")
        val firstName: String? = null,
        @SerializedName("last_name")
        val lastName: String? = null,

        @SerializedName("full_name")
        val fullName: String? = null,
        @SerializedName("short_bio")
        val shortBio: String? = null,

        val details: String? = null,
        val avatar: String? = null,

        @SerializedName("is_private")
        val isPrivate: Boolean = false,
        @SerializedName("join_date")
        val joinDate: Date?
)