package org.stepik.android.domain.course_benefits.repository

import io.reactivex.Maybe
import org.stepik.android.domain.course_benefits.model.CourseBenefit

interface CourseBenefitsRepository {
    fun getCourseBenefits(): Maybe<List<CourseBenefit>>
}