package org.stepik.android.domain.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefitByMonth

interface CourseBenefitByMonthsRepository {
    fun getCourseBenefitByMonths(courseId: Long): Single<List<CourseBenefitByMonth>>
}