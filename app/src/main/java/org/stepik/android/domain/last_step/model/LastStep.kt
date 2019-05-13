package org.stepik.android.domain.last_step.model

import android.os.Parcel
import android.os.Parcelable

class LastStep(
    val id: String,
    val unit: Long,
    val lesson: Long,
    val step: Long
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(unit)
        parcel.writeLong(lesson)
        parcel.writeLong(step)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LastStep> {
        override fun createFromParcel(parcel: Parcel): LastStep =
            LastStep(
                parcel.readString()!!,
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong()
            )

        override fun newArray(size: Int): Array<LastStep?> =
            arrayOfNulls(size)
    }
}