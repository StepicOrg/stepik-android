package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

data class CourseHeaderData(
    val courseId: Long,
    val course: Course,
    val title: String,
    val cover: String,

    val stats: CourseStats,
    val progress: Progress?,
    val enrollmentState: EnrollmentState
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(courseId)
        parcel.writeParcelable(course, flags)
        parcel.writeString(title)
        parcel.writeString(cover)
        parcel.writeParcelable(stats, flags)
        parcel.writeParcelable(progress, flags)
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
                parcel.readParcelable(CourseStats::class.java.classLoader)!!,
                parcel.readParcelable(Progress::class.java.classLoader),
                restoreEnrollmentState(parcel.readSerializable() as EnrollmentState)
            )

        /**
         * Reason is that deserialized [enrollmentState] object has different reference from one in [EnrollmentState]
         */
        private fun restoreEnrollmentState(enrollmentState: EnrollmentState): EnrollmentState =
            when (enrollmentState) {
                is EnrollmentState.Enrolled ->
                    EnrollmentState.Enrolled

                is EnrollmentState.NotEnrolledFree ->
                    EnrollmentState.NotEnrolledFree

                is EnrollmentState.NotEnrolledWeb ->
                    EnrollmentState.NotEnrolledWeb

                is EnrollmentState.Pending ->
                    EnrollmentState.Pending
            }

        override fun newArray(size: Int): Array<CourseHeaderData?> =
            arrayOfNulls(size)
    }
}
