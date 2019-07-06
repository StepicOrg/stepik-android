package org.stepik.android.model.comments

import com.google.gson.annotations.SerializedName

data class DiscussionProxy(
    @SerializedName("id")
    val id: String,
    @SerializedName("discussions")
    val discussions: List<Long>,

    @SerializedName("discussions_most_liked")
    val discussionsMostLiked: List<Long>,
    @SerializedName("discussions_most_active")
    val discussionsMostActive: List<Long>,
    @SerializedName("discussions_recent_activity")
    val discussionsRecentActivity: List<Long>
)