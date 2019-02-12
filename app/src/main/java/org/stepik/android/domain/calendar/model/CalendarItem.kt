package org.stepik.android.domain.calendar.model

import android.os.Parcel
import android.os.Parcelable

data class CalendarItem(
        var calendarId: Long = 0,
        var owner: String = "",
        var isPrimary: Boolean = false
) : Parcelable {


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(calendarId)
        dest.writeString(owner)
        dest.writeInt(if (isPrimary) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<CalendarItem> {
        override fun createFromParcel(parcel: Parcel): CalendarItem {
            return CalendarItem(
                    parcel.readLong(),
                    parcel.readString(),
                    parcel.readInt() == 1
            )
        }

        override fun newArray(size: Int): Array<CalendarItem?> {
            return arrayOfNulls(size)
        }
    }
}