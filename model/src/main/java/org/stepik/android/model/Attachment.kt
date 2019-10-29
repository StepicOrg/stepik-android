package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable

data class Attachment(
    val name: String,
    val size: Long,
    val url: String,
    val content: String,
    val type: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(size)
        parcel.writeString(url)
        parcel.writeString(content)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Attachment> {
        override fun createFromParcel(parcel: Parcel): Attachment =
            Attachment(
                parcel.readString()!!,
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readString()!!
            )

        override fun newArray(size: Int): Array<Attachment?> =
            arrayOfNulls(size)
    }
}