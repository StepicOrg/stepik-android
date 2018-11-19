package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable

data class CourseHeaderData(
    val courseId: Long,
    val title: String,
    val cover: String,
    val learnersCount: Long,

    val review: Double,
    val progress: Int,
    val isVerified: Boolean,
    val isEnrolled: Boolean
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(courseId)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeLong(learnersCount)
        parcel.writeDouble(review)
        parcel.writeInt(progress)
        parcel.writeByte(if (isVerified) 1 else 0)
        parcel.writeByte(if (isEnrolled) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseHeaderData> {
        override fun createFromParcel(parcel: Parcel): CourseHeaderData =
            CourseHeaderData(
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readLong(),
                parcel.readDouble(),
                parcel.readInt(),
                parcel.readByte() != 0.toByte(),
                parcel.readByte() != 0.toByte()
            )

        override fun newArray(size: Int): Array<CourseHeaderData?> =
            arrayOfNulls(size)
    }
}