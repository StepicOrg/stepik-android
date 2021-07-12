package org.stepik.android.remote.course_revenue.service

import io.reactivex.Maybe
import org.stepik.android.remote.course_revenue.model.CourseBenefitByMonthsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseBenefitByMonthsService {
    @GET(" /api/course-benefit-by-months")
    fun getCourseBenefitByMonths(@Query("course") courseId: Long): Maybe<CourseBenefitByMonthsResponse>
}