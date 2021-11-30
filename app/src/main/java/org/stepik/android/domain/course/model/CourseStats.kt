package org.stepik.android.domain.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Progress

@Parcelize
data class CourseStats(
    val review: Double,
    val learnersCount: Long,
    val readiness: Double,
    val progress: Progress?,
    val enrollmentState: EnrollmentState
) : Parcelable