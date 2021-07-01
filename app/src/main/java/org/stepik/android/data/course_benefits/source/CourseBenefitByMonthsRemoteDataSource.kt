package org.stepik.android.data.course_benefits.source

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRemoteDataSource {
    fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>>
}