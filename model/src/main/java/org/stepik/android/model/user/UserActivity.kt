package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName
import java.util.*

data class UserActivity(
    @SerializedName("id")
    val id: Long = -1,
    @SerializedName("pins")
    val pins: ArrayList<Long>
)