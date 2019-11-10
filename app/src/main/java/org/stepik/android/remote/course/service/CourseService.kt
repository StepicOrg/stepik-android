package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepic.droid.web.UserCoursesResponse
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseService {
    fun getCourses(page: Int, ids: LongArray): Call<CourseResponse>

    fun getCoursesReactive(page: Int, ids: LongArray): Single<CourseResponse>

    fun getCoursesReactive(ids: LongArray): Single<CourseResponse>

    @GET("api/user-courses")
    fun getUserCourses(@Query("page") page: Int): Single<UserCoursesResponse>

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    fun getPopularCourses(@Query("page") page: Int, @Query("language") language: String): Single<CourseResponse>
}