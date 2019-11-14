package org.stepik.android.remote.course_list.service

import io.reactivex.Single
import org.stepik.android.remote.course_list.model.CourseCollectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseListService {
    @GET("api/course-lists?platform=mobile")
    fun getCourseLists(@Query("language") language: String): Single<CourseCollectionsResponse>
}