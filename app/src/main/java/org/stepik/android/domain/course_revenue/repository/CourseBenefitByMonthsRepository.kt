package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth
import ru.nobird.app.core.model.PagedList

interface CourseBenefitByMonthsRepository {
    fun getCourseBenefitByMonths(courseId: Long, page: Int = 1): Single<PagedList<CourseBenefitByMonth>>
}