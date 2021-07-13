package org.stepik.android.data.course_revenue.source

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary

interface CourseBenefitSummariesRemoteDataSource {
    fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>>
}