package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.UserActivity
import org.stepik.android.model.Meta

class UserActivityResponse(
        val meta: Meta?,
        @SerializedName("user-activities")
        val userActivities: List<UserActivity>
)