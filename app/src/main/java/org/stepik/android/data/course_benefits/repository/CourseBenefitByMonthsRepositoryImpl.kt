package org.stepik.android.data.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.data.course_benefits.source.CourseBenefitByMonthsRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefitByMonth
import org.stepik.android.domain.course_benefits.repository.CourseBenefitByMonthsRepository
import javax.inject.Inject

class CourseBenefitByMonthsRepositoryImpl
@Inject
constructor(
    private val courseBenefitByMonthsRemoteDataSource: CourseBenefitByMonthsRemoteDataSource
) : CourseBenefitByMonthsRepository {
    override fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>> =
        courseBenefitByMonthsRemoteDataSource.getCourseBenefitByMonths(courseId)
}