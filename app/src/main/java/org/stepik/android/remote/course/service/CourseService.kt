package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseService {
    @GET("api/courses")
    fun getCoursesReactive(@Query("page") page: Int, @Query("ids[]") ids: LongArray): Single<CourseResponse>

    @GET("api/courses")
    fun getCoursesReactive(@Query("ids[]") ids: LongArray): Single<CourseResponse>

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    fun getPopularCourses(@Query("page") page: Int, @Query("language") language: String): Single<CourseResponse>
}