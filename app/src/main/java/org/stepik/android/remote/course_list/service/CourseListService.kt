package org.stepik.android.remote.course_list.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseListService {
    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    fun getPopularCourses(@Query("page") page: Int, @Query("language") language: String): Single<CourseResponse>
}