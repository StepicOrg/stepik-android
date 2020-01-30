package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable

data class CourseStats(
    val review: Double,
    val learnersCount: Long,
    val readiness: Double
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(review)
        parcel.writeLong(learnersCount)
        parcel.writeDouble(readiness)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseStats> {
        override fun createFromParcel(parcel: Parcel): CourseStats =
            CourseStats(
                parcel.readDouble(),
                parcel.readLong(),
                parcel.readDouble()
            )

        override fun newArray(size: Int): Array<CourseStats?> =
            arrayOfNulls(size)
    }
}