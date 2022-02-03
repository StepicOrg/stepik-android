package org.stepik.android.remote.course_revenue

import io.reactivex.Single
import org.stepik.android.data.course_revenue.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course_revenue.model.CourseBenefitsResponse
import org.stepik.android.remote.course_revenue.service.CourseBenefitsService
import ru.nobird.app.core.model.PagedList
import javax.inject.Inject

class CourseBenefitsRemoteDataSourceImpl
@Inject
constructor(
    private val courseBenefitsService: CourseBenefitsService
) : CourseBenefitsRemoteDataSource {
    override fun getCourseBenefits(courseId: Long, page: Int): Single<PagedList<CourseBenefit>> =
        courseBenefitsService
            .getCourseBenefits(courseId, page)
            .map { it.toPagedList(CourseBenefitsResponse::courseBenefits) }
}