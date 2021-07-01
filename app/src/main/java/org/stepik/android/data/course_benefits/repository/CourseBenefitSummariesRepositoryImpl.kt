package org.stepik.android.data.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.data.course_benefits.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary
import org.stepik.android.domain.course_benefits.repository.CourseBenefitSummariesRepository
import javax.inject.Inject

class CourseBenefitSummariesRepositoryImpl
@Inject
constructor(
    private val courseBenefitSummariesRemoteDataSource: CourseBenefitSummariesRemoteDataSource
) : CourseBenefitSummariesRepository {
    override fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>> =
        courseBenefitSummariesRemoteDataSource.getCourseBenefitSummaries(courseId)
}