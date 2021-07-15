package org.stepik.android.remote.course_revenue

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.remote.course_revenue.model.CourseBenefitSummariesResponse
import org.stepik.android.remote.course_revenue.service.CourseBenefitSummariesService
import javax.inject.Inject

class CourseBenefitSummariesRemoteDataSourceImpl
@Inject
constructor(
    private val courseBenefitSummariesService: CourseBenefitSummariesService
) : CourseBenefitSummariesRemoteDataSource {
    override fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>> =
        courseBenefitSummariesService
            .getCourseBenefitSummaries(courseId)
            .map(CourseBenefitSummariesResponse::courseBenefitSummaries)
}