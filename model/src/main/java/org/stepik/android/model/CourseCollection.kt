package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable

data class CourseCollection(
    val id: Long,
    val position: Int,
    val title: String,
    val language: String,
    val courses: LongArray,
    val description: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
       parcel.writeLong(id)
       parcel.writeInt(position)
       parcel.writeString(title)
       parcel.writeString(language)
       parcel.writeLongArray(courses)
       parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseCollection> {
        override fun createFromParcel(parcel: Parcel): CourseCollection =
            CourseCollection(
                parcel.readLong(),
                parcel.readInt(),
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.createLongArray()!!,
                parcel.readString()!!
            )

        override fun newArray(size: Int): Array<CourseCollection?> = arrayOfNulls(size)
    }
}