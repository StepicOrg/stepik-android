package org.stepik.android.remote.course_benefits.service

import io.reactivex.Single
import org.stepik.android.remote.course_benefits.model.CourseBenefitsResponse

interface CourseBenefitsService {
    fun getCourseBenefits(): Single<CourseBenefitsResponse>
}