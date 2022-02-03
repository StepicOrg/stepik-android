package org.stepik.android.data.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitByMonthsRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth
import org.stepik.android.domain.course_revenue.repository.CourseBenefitByMonthsRepository
import ru.nobird.app.core.model.PagedList
import javax.inject.Inject

class CourseBenefitByMonthsRepositoryImpl
@Inject
constructor(
    private val courseBenefitByMonthsRemoteDataSource: CourseBenefitByMonthsRemoteDataSource
) : CourseBenefitByMonthsRepository {
    override fun getCourseBenefitByMonths(courseId: Long, page: Int): Single<PagedList<CourseBenefitByMonth>> =
        courseBenefitByMonthsRemoteDataSource.getCourseBenefitByMonths(courseId)
}