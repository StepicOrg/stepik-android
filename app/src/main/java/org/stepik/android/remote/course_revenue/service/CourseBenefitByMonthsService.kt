package org.stepik.android.remote.course_revenue.service

import io.reactivex.Single
import org.stepik.android.remote.course_revenue.model.CourseBenefitByMonthsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseBenefitByMonthsService {
    @GET(" /api/course-benefit-by-months")
    fun getCourseBenefitByMonths(@Query("course") courseId: Long, @Query("page") page: Int): Single<CourseBenefitByMonthsResponse>
}