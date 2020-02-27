package org.stepik.android.domain.course.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Progress

data class CourseStats(
    val review: Double,
    val learnersCount: Long,
    val readiness: Double,
    val progress: Progress?,
    val enrollmentState: EnrollmentState
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(review)
        parcel.writeLong(learnersCount)
        parcel.writeDouble(readiness)
        parcel.writeParcelable(progress, flags)
        parcel.writeSerializable(enrollmentState)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseStats> {
        override fun createFromParcel(parcel: Parcel): CourseStats =
            CourseStats(
                parcel.readDouble(),
                parcel.readLong(),
                parcel.readDouble(),
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

        override fun newArray(size: Int): Array<CourseStats?> =
            arrayOfNulls(size)
    }
}