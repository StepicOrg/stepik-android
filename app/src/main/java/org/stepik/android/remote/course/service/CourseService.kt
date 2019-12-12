package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseService {
    @GET("api/courses")
    fun getCourses(@Query("ids[]") ids: LongArray): Single<CourseResponse>
}