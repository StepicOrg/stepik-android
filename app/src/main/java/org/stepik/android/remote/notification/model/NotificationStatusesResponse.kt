package org.stepik.android.remote.notification.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class NotificationStatusesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("notification-statuses")
    val notificationStatuses: List<NotificationStatuses>?
) : MetaResponse