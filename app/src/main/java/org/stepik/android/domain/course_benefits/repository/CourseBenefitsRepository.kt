package org.stepik.android.domain.course_benefits.repository

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefit

interface CourseBenefitsRepository {
    fun getCourseBenefits(): Single<List<CourseBenefit>>
}