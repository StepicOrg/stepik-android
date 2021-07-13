package org.stepik.android.data.course_revenue.source

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRemoteDataSource {
    fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>>
}