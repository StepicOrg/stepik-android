package org.stepik.android.data.course_revenue.source

import io.reactivex.Maybe
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRemoteDataSource {
    fun getCourseBenefitByMonths(courseId: Long): Maybe<List<CourseBenefitByMonth>>
}