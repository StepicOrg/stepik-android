package org.stepik.android.remote.user_activity.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.user.UserActivitySummary
import org.stepik.android.remote.base.model.MetaResponse

class UserActivitySummaryResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("user-activity-summaries")
    val userActivitySummaries: List<UserActivitySummary>
) : MetaResponse