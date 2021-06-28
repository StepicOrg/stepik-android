package org.stepik.android.data.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.data.course_benefits.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefit
import org.stepik.android.domain.course_benefits.repository.CourseBenefitsRepository
import javax.inject.Inject

class CourseBenefitsRepositoryImpl
@Inject
constructor(
    private val courseBenefitsRemoteDataSource: CourseBenefitsRemoteDataSource
) : CourseBenefitsRepository {
    override fun getCourseBenefits(): Single<List<CourseBenefit>> =
        courseBenefitsRemoteDataSource.getCourseBenefits()
}