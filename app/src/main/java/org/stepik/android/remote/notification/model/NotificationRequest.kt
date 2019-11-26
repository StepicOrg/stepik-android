package org.stepik.android.remote.notification.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.notifications.model.Notification

class NotificationRequest(
    @SerializedName("notification")
    val notification: Notification
)