package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRepository {
    fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>>
}