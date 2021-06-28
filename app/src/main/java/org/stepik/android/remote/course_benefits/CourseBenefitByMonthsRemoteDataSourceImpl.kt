package org.stepik.android.remote.course_benefits

import io.reactivex.Single
import org.stepik.android.data.course_benefits.source.CourseBenefitByMonthsRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefitByMonth
import org.stepik.android.remote.course_benefits.model.CourseBenefitByMonthsResponse
import org.stepik.android.remote.course_benefits.service.CourseBenefitByMonthsService
import javax.inject.Inject

class CourseBenefitByMonthsRemoteDataSourceImpl
@Inject
constructor(
    private val courseBenefitRemoteByMonthsService: CourseBenefitByMonthsService
) : CourseBenefitByMonthsRemoteDataSource {
    override fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>> =
        courseBenefitRemoteByMonthsService
            .getCourseBenefitByMonths(courseId)
            .map(CourseBenefitByMonthsResponse::courseBenefitByMonths)
}