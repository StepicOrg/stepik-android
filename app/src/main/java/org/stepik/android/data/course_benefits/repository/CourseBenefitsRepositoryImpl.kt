package org.stepik.android.data.course_benefits.repository

import io.reactivex.Maybe
import org.stepik.android.data.course_benefits.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefit
import org.stepik.android.domain.course_benefits.repository.CourseBenefitsRepository
import javax.inject.Inject

class CourseBenefitsRepositoryImpl
@Inject
constructor(
    private val courseBenefitsRemoteDataSource: CourseBenefitsRemoteDataSource
) : CourseBenefitsRepository {
    override fun getCourseBenefits(): Maybe<List<CourseBenefit>> =
        courseBenefitsRemoteDataSource.getCourseBenefits()
}