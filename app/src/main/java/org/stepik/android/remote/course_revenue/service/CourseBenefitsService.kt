package org.stepik.android.remote.course_revenue.service

import io.reactivex.Single
import org.stepik.android.remote.course_revenue.model.CourseBenefitsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseBenefitsService {
    @GET("/api/course-benefits")
    fun getCourseBenefits(@Query("course") courseId: Long, @Query("page") page: Int): Single<CourseBenefitsResponse>
}