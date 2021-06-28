package org.stepik.android.remote.course_benefits.service

import io.reactivex.Single
import org.stepik.android.remote.course_benefits.model.CourseBenefitByMonthsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseBenefitByMonthsService {
    @GET(" /api/course-benefit-by-months")
    fun getCourseBenefitByMonths(@Query("course") courseId: Long): Single<CourseBenefitByMonthsResponse>
}