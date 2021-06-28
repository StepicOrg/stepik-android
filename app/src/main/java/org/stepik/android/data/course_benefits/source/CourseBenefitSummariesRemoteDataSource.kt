package org.stepik.android.data.course_benefits.source

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary

interface CourseBenefitSummariesRemoteDataSource {
    fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>>
}