package org.stepik.android.remote.course_reviews.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.course_reviews.model.CourseReviewRequest
import org.stepik.android.remote.course_reviews.model.CourseReviewsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseReviewService {
    @GET("api/course-reviews")
    fun getCourseReviewsByCourseId(
        @Query("course") course: Long,
        @Query("page") page: Int
    ): Single<CourseReviewsResponse>

    @GET("api/course-reviews")
    fun getCourseReviewByCourseIdAndUserId(
        @Query("course") course: Long,
        @Query("user") user: Long
    ): Single<CourseReviewsResponse>

    @POST("api/course-reviews")
    fun createCourseReview(
        @Body request: CourseReviewRequest
    ): Single<CourseReviewsResponse>

    @PUT("api/course-reviews/{courseReviewId}")
    fun updateCourseReview(
        @Path("courseReviewId") courseReviewId: Long,
        @Body request: CourseReviewRequest
    ): Single<CourseReviewsResponse>

    @DELETE("api/course-reviews/{courseReviewId}")
    fun removeCourseReview(
        @Path("courseReviewId") courseReviewId: Long
    ): Completable
}