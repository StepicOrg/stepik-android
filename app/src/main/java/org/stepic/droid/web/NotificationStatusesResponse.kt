package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Meta
import org.stepic.droid.notifications.model.NotificationStatuses

class NotificationStatusesResponse(
        meta: Meta,
        @SerializedName("notification-statuses")
        val notificationStatuses: List<NotificationStatuses>?
) : MetaResponseBase(meta)