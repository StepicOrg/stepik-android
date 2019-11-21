package org.stepik.android.remote.user_activity.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.user.UserActivity
import org.stepik.android.remote.base.model.MetaResponse

class UserActivityResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("user-activities")
    val userActivities: List<UserActivity>
) : MetaResponse