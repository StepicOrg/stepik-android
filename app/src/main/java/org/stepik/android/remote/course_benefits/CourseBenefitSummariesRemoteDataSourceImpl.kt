package org.stepik.android.remote.course_benefits

import io.reactivex.Single
import org.stepik.android.data.course_benefits.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary
import org.stepik.android.remote.course_benefits.model.CourseBenefitSummariesResponse
import org.stepik.android.remote.course_benefits.service.CourseBenefitSummariesService
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