package org.stepik.android.data.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBeneficiariesRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.repository.CourseBeneficiariesRepository
import javax.inject.Inject

class CourseBeneficiariesRepositoryImpl
@Inject
constructor(
    private val courseBeneficiariesRemoteDataSource: CourseBeneficiariesRemoteDataSource
) : CourseBeneficiariesRepository {
    override fun getCourseBeneficiary(courseId: Long, userId: Long): Single<CourseBeneficiary> =
        courseBeneficiariesRemoteDataSource.getCourseBeneficiary(courseId, userId)
}