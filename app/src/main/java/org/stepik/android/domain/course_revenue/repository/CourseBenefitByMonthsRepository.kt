package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Maybe
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRepository {
    fun getCourseBenefitByMonths(courseId: Long): Maybe<List<CourseBenefitByMonth>>
}