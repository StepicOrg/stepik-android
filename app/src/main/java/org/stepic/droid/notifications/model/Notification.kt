package org.stepic.droid.notifications.model

import com.google.gson.annotations.SerializedName

class Notification(
    val id: Long? = 0,
    @SerializedName("is_unread")
    var isUnread: Boolean? = null,
    @SerializedName("is_muted")
    val isMuted: Boolean? = null,
    @SerializedName("is_favorite")
    val isFavourite: Boolean? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("type")
    val type: NotificationType = NotificationType.other,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("html_text")
    var htmlText: String? = null,
    @SerializedName("action")
    val action: String? = null,

    var courseId: Long? = null,
    var userAvatarUrl: String? = null,
    var notificationText: CharSequence? = null,
    var dateGroup: Int = 0
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