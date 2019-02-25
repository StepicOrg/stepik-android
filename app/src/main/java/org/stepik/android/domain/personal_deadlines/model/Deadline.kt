package org.stepik.android.domain.personal_deadlines.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.Date

class Deadline(
    @SerializedName("section")
    val section: Long,
    @SerializedName("deadline")
    val deadline: Date
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(section)
        parcel.writeLong(deadline.time)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Deadline> {
        override fun createFromParcel(parcel: Parcel): Deadline =
            Deadline(
                parcel.readLong(),
                Date(parcel.readLong())
            )
        override fun newArray(size: Int): Array<Deadline?> =
            arrayOfNulls(size)
    }
}