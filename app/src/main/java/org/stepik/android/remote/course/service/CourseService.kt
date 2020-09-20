package org.stepik.android.remote.course.service

import io.reactivex.Single
import org.stepik.android.remote.course.model.CourseResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface CourseService {
    @GET("api/courses")
    fun getCourses(@Query("ids[]") ids: List<Long>): Single<CourseResponse>

    @GET("api/courses")
    fun getCourses(@QueryMap query: Map<String, String>): Single<CourseResponse>
}