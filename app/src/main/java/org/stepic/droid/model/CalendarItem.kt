package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

class CalendarItem() : Parcelable {
    var calendarId: Long = 0
    var owner: String = ""
    var isPrimary: Boolean = false

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.calendarId)
        dest.writeString(this.owner)
        dest.writeByte(if (isPrimary) 1.toByte() else 0.toByte())
    }

    protected constructor(input: Parcel) : this() {
        this.calendarId = input.readLong()
        this.owner = input.readString()
        this.isPrimary = input.readByte().toInt() != 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CalendarItem> = object : Parcelable.Creator<CalendarItem> {
            override fun createFromParcel(source: Parcel): CalendarItem {
                return CalendarItem(source)
            }

            override fun newArray(size: Int): Array<CalendarItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(calendarId: Long, owner: String, primary: Boolean) : this() {
        this.calendarId = calendarId
        this.owner = owner
        this.isPrimary = primary
    }

}
