package org.stepic.droid.notifications.model

import com.google.gson.annotations.SerializedName

data class Notification(
        var id: Long? = null,
        @SerializedName("is_unread")
        var is_unread: Boolean? = null,
        @SerializedName("is_muted")
        var isMuted: Boolean? = null,
        @SerializedName("is_favorite")
        var isFavourite: Boolean? = null,
        var time: String? = null,
        var type: NotificationType? = null,
        val level: String? = null,
        val priority: String? = null,
        @SerializedName("html_text")
        val htmlText: String? = null
)

enum class NotificationType {
    @SerializedName("comments")
    comments,
    @SerializedName("review")
    review,
    @SerializedName("teach")
    teach,
    @SerializedName("learn")
    learn,
    @SerializedName("default")
    default
}