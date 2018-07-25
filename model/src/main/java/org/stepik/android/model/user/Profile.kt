package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName

data class Profile(
        val id: Long = 0,

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
        @SerializedName("is_guest")
        val isGuest: Boolean = false,

        @SerializedName("email_addresses")
        val emailAddresses: LongArray? = null
)