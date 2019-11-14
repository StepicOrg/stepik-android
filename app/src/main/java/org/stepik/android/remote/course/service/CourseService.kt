package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.UserCoursesResponse
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseService {
    @GET("api/courses")
    fun getCourses(@Query("page") page: Int, @Query("ids[]") ids: LongArray): Call<CourseResponse>

    @GET("api/courses")
    fun getCourses(@Query("ids[]") courseIds: LongArray): Call<CourseResponse>

    @GET("api/courses")
    fun getCoursesReactive(@Query("page") page: Int, @Query("ids[]") ids: LongArray): Single<CourseResponse>

    @GET("api/courses")
    fun getCoursesReactive(@Query("ids[]") ids: LongArray): Single<CourseResponse>

    @GET("api/user-courses")
    fun getUserCourses(@Query("page") page: Int): Single<UserCoursesResponse>

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    fun getPopularCourses(@Query("page") page: Int, @Query("language") language: String): Single<CourseResponse>
}