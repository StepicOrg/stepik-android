package org.stepik.android.data.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.domain.course_revenue.repository.CourseBenefitsRepository
import javax.inject.Inject

class CourseBenefitsRepositoryImpl
@Inject
constructor(
    private val courseBenefitsRemoteDataSource: CourseBenefitsRemoteDataSource
) : CourseBenefitsRepository {
    override fun getCourseBenefits(courseId: Long): Single<List<CourseBenefit>> =
        courseBenefitsRemoteDataSource.getCourseBenefits(courseId)
}