package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CourseActions(
    @SerializedName("create_announcements")
    val createAnnouncements: String?,
    @SerializedName("view_revenue")
    val viewRevenue: ViewRevenue?
) : Parcelable