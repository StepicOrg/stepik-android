package org.stepik.android.remote.course_recommendations.service

import io.reactivex.Single
import org.stepik.android.remote.course_recommendations.model.CourseRecommendationsResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CourseRecommendationsService {
    @GET("api/course-recommendations?platform=mobile,android")
    fun getCourseRecommendations(@QueryMap query: Map<String, String>): Single<CourseRecommendationsResponse>
}