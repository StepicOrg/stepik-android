package org.stepik.android.remote.course_revenue

import io.reactivex.Maybe
import org.stepik.android.data.course_revenue.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.remote.course_revenue.model.CourseBenefitsResponse
import org.stepik.android.remote.course_revenue.service.CourseBenefitsService
import javax.inject.Inject

class CourseBenefitsRemoteDataSourceImpl
@Inject
constructor(
    private val courseBenefitsService: CourseBenefitsService
) : CourseBenefitsRemoteDataSource {
    override fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefit>> =
        courseBenefitsService
            .getCourseBenefits(courseId)
            .map(CourseBenefitsResponse::courseBenefits)
}