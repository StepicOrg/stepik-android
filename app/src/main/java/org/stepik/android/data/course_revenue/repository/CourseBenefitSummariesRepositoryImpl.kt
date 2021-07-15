package org.stepik.android.data.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.domain.course_revenue.repository.CourseBenefitSummariesRepository
import javax.inject.Inject

class CourseBenefitSummariesRepositoryImpl
@Inject
constructor(
    private val courseBenefitSummariesRemoteDataSource: CourseBenefitSummariesRemoteDataSource
) : CourseBenefitSummariesRepository {
    override fun getCourseBenefitSummaries(courseId: Long): Single<List<CourseBenefitSummary>> =
        courseBenefitSummariesRemoteDataSource.getCourseBenefitSummaries(courseId)
}