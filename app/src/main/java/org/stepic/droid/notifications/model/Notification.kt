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
        var type: NotificationType = NotificationType.other,
        var level: String? = null,
        var priority: String? = null,
        @SerializedName("html_text")
        var htmlText: String? = null,
        var action: String? = null,
        var course_id: Long? = null,
        var userAvatarUrl: String? = null,
        var notificationText: CharSequence? = null
)

enum class NotificationType(val channel: StepikNotificationChannel) {

    @SerializedName("comments")
    comments(StepikNotificationChannel.comments),

    @SerializedName("review")
    review(StepikNotificationChannel.review),

    @SerializedName("teach")
    teach(StepikNotificationChannel.teach),

    @SerializedName("learn")
    learn(StepikNotificationChannel.learn),

    @SerializedName("default")
    other(StepikNotificationChannel.other)
}