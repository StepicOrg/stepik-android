package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Meta
import org.stepic.droid.model.UserActivity

data class UserActivityResponse(
        val meta: Meta?,
        @SerializedName("user-activities")
        val userActivities: List<UserActivity>)