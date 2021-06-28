package org.stepik.android.data.course_benefits.source

import io.reactivex.Single
import org.stepik.android.domain.course_benefits.model.CourseBenefit

interface CourseBenefitsRemoteDataSource {
    fun getCourseBenefits(): Single<List<CourseBenefit>>
}