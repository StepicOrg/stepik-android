package org.stepik.android.remote.course_collection.service

import io.reactivex.Single
import org.stepik.android.remote.course_collection.model.CourseCollectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface CourseCollectionService {
    @GET("api/course-lists")
    fun getCourseCollections(@Query("ids[]") ids: List<Long>): Single<CourseCollectionsResponse>

    @GET("api/course-lists")
    fun getCourseCollections(@QueryMap query: Map<String, String>): Single<CourseCollectionsResponse>
}