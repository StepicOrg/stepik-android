package org.stepik.android.remote.course_benefits

import io.reactivex.Maybe
import org.stepik.android.data.course_benefits.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefit
import org.stepik.android.remote.course_benefits.model.CourseBenefitsResponse
import org.stepik.android.remote.course_benefits.service.CourseBenefitsService
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