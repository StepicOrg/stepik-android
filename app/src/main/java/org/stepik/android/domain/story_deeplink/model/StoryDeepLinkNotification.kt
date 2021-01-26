package org.stepik.android.domain.story_deeplink.model

import com.google.gson.annotations.SerializedName

data class StoryDeepLinkNotification(
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("story_url")
    val storyUrl: String
)