package org.stepik.android.domain.calendar.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

data class CalendarItem(
    val calendarId: Long = 0,
    val owner: String = "",
    val isPrimary: Boolean = false
) : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.calendarId)
        dest.writeString(this.owner)
        dest.writeBoolean(this.isPrimary)
    }

    companion object CREATOR : Parcelable.Creator<CalendarItem> {
        override fun createFromParcel(parcel: Parcel): CalendarItem =
            CalendarItem(
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readBoolean()
            )

        override fun newArray(size: Int): Array<CalendarItem?> =
            arrayOfNulls(size)
    }
}