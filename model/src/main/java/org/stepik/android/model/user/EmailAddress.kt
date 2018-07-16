package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName

data class EmailAddress(
        val id: Long = 0,
        val user: Long = 0,
        val email: String? = null,

        @SerializedName("is_verified")
        val isVerified: Boolean = false,
        @SerializedName("is_primary")
        val isPrimary: Boolean = false
)