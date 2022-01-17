package org.stepik.android.view.personal_deadlines.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeadlinesNotificationData(
    val course: Long,
    val hours: Long
) : Parcelable
