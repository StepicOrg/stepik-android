package org.stepik.android.domain.calendar.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CalendarItem(
    val calendarId: Long = 0,
    val owner: String = "",
    val isPrimary: Boolean = false
) : Parcelable