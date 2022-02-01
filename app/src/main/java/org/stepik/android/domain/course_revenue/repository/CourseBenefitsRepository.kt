package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import ru.nobird.app.core.model.PagedList

interface CourseBenefitsRepository {
    fun getCourseBenefits(courseId: Long, page: Int = 1): Single<PagedList<CourseBenefit>>
}