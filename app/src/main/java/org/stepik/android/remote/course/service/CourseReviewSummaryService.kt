package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseReviewSummaryService {
    @GET("api/course-review-summaries")
    fun getCourseReviewSummaries(@Query("ids[]") reviewSummaryIds: LongArray): Single<CourseReviewSummaryResponse>
}