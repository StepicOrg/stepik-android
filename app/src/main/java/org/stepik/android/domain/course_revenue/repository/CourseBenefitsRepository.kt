package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefit

interface CourseBenefitsRepository {
    fun getCourseBenefits(courseId: Long): Single<List<CourseBenefit>>
}