package org.stepik.android.remote.user_courses.service

import io.reactivex.Single
import org.stepik.android.remote.user_courses.model.UserCoursesRequest
import org.stepik.android.remote.user_courses.model.UserCoursesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserCoursesService {
    @GET("api/user-courses")
    fun getUserCourses(@Query("page") page: Int): Single<UserCoursesResponse>

    @GET("api/user-courses")
    fun getUserCourseByCourseId(@Query("course") course: Long): Single<UserCoursesResponse>

    @PUT("api/user-courses/{id}")
    fun saveUserCourse(@Path("id") userCourseId: Long, @Body request: UserCoursesRequest): Single<UserCoursesResponse>
}