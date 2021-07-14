package org.stepik.android.data.course_revenue.source

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import ru.nobird.android.core.model.PagedList

interface CourseBenefitsRemoteDataSource {
    fun getCourseBenefits(courseId: Long, page: Int = 1): Single<PagedList<CourseBenefit>>
}