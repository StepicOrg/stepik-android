package org.stepik.android.domain.course.mapper

import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import javax.inject.Inject

class CourseStatsMapper
@Inject
constructor() {
    inline fun mutateEnrolledState(stats: CourseStats, mutation: EnrollmentState.Enrolled.() -> EnrollmentState): CourseStats =
        if (stats.enrollmentState is EnrollmentState.Enrolled) {
            stats.copy(enrollmentState = stats.enrollmentState.mutation())
        } else {
            stats
        }
}