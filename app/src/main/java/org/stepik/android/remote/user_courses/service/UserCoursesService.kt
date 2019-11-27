package org.stepik.android.remote.user_courses.service

import io.reactivex.Single
import org.stepik.android.remote.user_courses.model.UserCoursesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserCoursesService {
    @GET("api/user-courses")
    fun getUserCourses(@Query("page") page: Int): Single<UserCoursesResponse>
}