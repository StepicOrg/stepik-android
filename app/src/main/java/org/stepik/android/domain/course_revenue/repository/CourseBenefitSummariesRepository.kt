package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary

interface CourseBenefitSummariesRepository {
    fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>>
}