package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Maybe
import org.stepik.android.domain.course_revenue.model.CourseBenefit

interface CourseBenefitsRepository {
    fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefit>>
}