package org.stepik.android.remote.user_courses.service

import io.reactivex.Completable
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

    @PUT("api/user-courses/{id}")
    fun toggleUserCourse(@Path("id") userCourseId: Long, @Body request: UserCoursesRequest): Completable
}