package org.stepik.android.domain.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary

interface CourseBenefitSummariesRepository {
    fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>>
}