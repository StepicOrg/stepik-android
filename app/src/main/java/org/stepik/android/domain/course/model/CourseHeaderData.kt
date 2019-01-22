package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Course

data class CourseHeaderData(
    val courseId: Long,
    val course: Course,
    val title: String,
    val cover: String,
    val learnersCount: Long,

    val review: Double,
    val progress: Long?,
    val readiness: Double,
    val enrollmentState: EnrollmentState
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(courseId)
        parcel.writeParcelable(course, flags)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeLong(learnersCount)
        parcel.writeDouble(review)
        parcel.writeValue(progress)
        parcel.writeDouble(readiness)
        parcel.writeSerializable(enrollmentState)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseHeaderData> {
        override fun createFromParcel(parcel: Parcel): CourseHeaderData =
            CourseHeaderData(
                parcel.readLong(),
                parcel.readParcelable(Course::class.java.classLoader)!!,
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readLong(),
                parcel.readDouble(),
                parcel.readValue(Long::class.java.classLoader) as Long?,
                parcel.readDouble(),
                parcel.readSerializable() as EnrollmentState
            )

        override fun newArray(size: Int): Array<CourseHeaderData?> =
            arrayOfNulls(size)
    }
}