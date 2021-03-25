package org.stepik.android.remote.course_recommendations.service

import io.reactivex.Maybe
import org.stepik.android.remote.course_recommendations.model.CourseRecommendationsResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CourseRecommendationsService {
    // TODO APPS-3254 use this when feature is complete
    // @GET("api/course-recommendations?platform=mobile,android")
    @GET("api/course-recommendations?platform=sunion_plugin")
    fun getCourseRecommendations(@QueryMap query: Map<String, String>): Maybe<CourseRecommendationsResponse>
}