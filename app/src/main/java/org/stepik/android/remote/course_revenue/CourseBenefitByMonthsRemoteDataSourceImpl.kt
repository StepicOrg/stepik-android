package org.stepik.android.remote.course_revenue

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitByMonthsRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course_revenue.model.CourseBenefitByMonthsResponse
import org.stepik.android.remote.course_revenue.service.CourseBenefitByMonthsService
import ru.nobird.android.core.model.PagedList
import javax.inject.Inject

class CourseBenefitByMonthsRemoteDataSourceImpl
@Inject
constructor(
    private val courseBenefitRemoteByMonthsService: CourseBenefitByMonthsService
) : CourseBenefitByMonthsRemoteDataSource {
    override fun getCourseBenefitByMonths(courseId: Long, page: Int): Single<PagedList<CourseBenefitByMonth>> =
        courseBenefitRemoteByMonthsService
            .getCourseBenefitByMonths(courseId, page)
            .map { it.toPagedList(CourseBenefitByMonthsResponse::courseBenefitByMonths) }
}