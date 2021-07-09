package org.stepik.android.remote.course_benefits.service

import io.reactivex.Maybe
import org.stepik.android.remote.course_benefits.model.CourseBenefitsResponse
import retrofit2.http.GET

interface CourseBenefitsService {
    @GET("/api/course-benefits")
    fun getCourseBenefits(): Maybe<CourseBenefitsResponse>
}