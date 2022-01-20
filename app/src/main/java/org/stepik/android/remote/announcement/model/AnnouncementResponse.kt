package org.stepik.android.remote.announcement.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class AnnouncementResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("announcements")
    val announcements: List<Announcement>
) : MetaResponse