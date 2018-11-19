package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable

data class CourseHeaderData(
    val courseId: Long,
    val title: String,
    val cover: String,
    val learnersCount: Long,

    val review: Double,
    val progress: Int?,
    val isFeatured: Boolean,
    val enrollmentState: EnrollmentState
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(courseId)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeLong(learnersCount)
        parcel.writeDouble(review)
        parcel.writeValue(progress)
        parcel.writeByte(if (isFeatured) 1 else 0)
        parcel.writeInt(enrollmentState.ordinal)
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
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readByte() != 0.toByte(),
                EnrollmentState.values()[parcel.readInt()]
            )

        override fun newArray(size: Int): Array<CourseHeaderData?> =
            arrayOfNulls(size)
    }
}