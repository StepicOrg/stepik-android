package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

class AchievementNotification(
        @SerializedName("user")
        val user: Long,

        @SerializedName("achievement")
        val achievement: Int,

        @SerializedName("kind")
        val kind: String
)