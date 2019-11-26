package org.stepik.android.remote.course_collection.service

import io.reactivex.Single
import org.stepik.android.remote.course_collection.model.CourseCollectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseCollectionService {
    @GET("api/course-lists?platform=mobile")
    fun getCourseCollectionList(@Query("language") language: String): Single<CourseCollectionsResponse>
}