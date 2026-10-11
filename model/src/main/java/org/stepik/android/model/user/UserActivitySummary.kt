package org.stepik.android.model.user

import com.google.gson.annotations.SerializedName

data class UserActivitySummary(
    @SerializedName("id")
    val id: Long = -1,
    @SerializedName("recent_strike")
    val recentStrike: Int = 0,
    @SerializedName("solved_today")
    val solvedToday: Int = 0,
    @SerializedName("max_strike")
    val maxStrike: Int = 0,
    @SerializedName("solved")
    val solved: Int = 0,
    @SerializedName("days")
    val days: Int = 0,
    @SerializedName("pins")
    val pins: List<Long> = emptyList()
)