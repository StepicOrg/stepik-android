package org.stepik.android.remote.course_revenue.service

import io.reactivex.Single
import org.stepik.android.remote.course_revenue.model.CourseBeneficiariesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseBeneficiariesService {
    @GET("/api/course-beneficiaries")
    fun getCourseBeneficiaries(@Query("course") courseId: Long, @Query("user") userId: Long): Single<CourseBeneficiariesResponse>
}