package org.stepik.android.remote.lesson.service

import io.reactivex.Single
import org.stepik.android.remote.lesson.model.LessonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LessonService {
    @GET("api/lessons")
    fun getLessons(@Query("ids[]") lessons: List<Long>): Single<LessonResponse>
}