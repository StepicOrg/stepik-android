package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Course

data class CourseHeaderData(
    val courseId: Long,
    val course: Course,
    val title: String,
    val cover: String,

    val stats: CourseStats,
    val localSubmissionsCount: Int
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(courseId)
        parcel.writeParcelable(course, flags)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeParcelable(stats, flags)
        parcel.writeInt(localSubmissionsCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseHeaderData> {
        override fun createFromParcel(parcel: Parcel): CourseHeaderData =
            CourseHeaderData(
                parcel.readLong(),
                parcel.readParcelable(Course::class.java.classLoader)!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readParcelable(CourseStats::class.java.classLoader)!!,
                parcel.readInt()
            )

        override fun newArray(size: Int): Array<CourseHeaderData?> =
            arrayOfNulls(size)
    }
}
