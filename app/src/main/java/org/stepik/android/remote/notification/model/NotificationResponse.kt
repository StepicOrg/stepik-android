package org.stepik.android.remote.notification.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.notifications.model.Notification
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class NotificationResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("notifications")
    val notifications: List<Notification>
) : MetaResponse