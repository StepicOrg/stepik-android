package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName
import ru.nobird.android.core.model.Identifiable

data class EmailAddress(
    @SerializedName("id")
    override val id: Long = 0,
    @SerializedName("user")
    val user: Long = 0,
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_primary")
    val isPrimary: Boolean = false
) : Identifiable<Long>