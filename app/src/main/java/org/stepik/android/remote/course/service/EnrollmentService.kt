package org.stepik.android.remote.course.service

import io.reactivex.Completable
import org.stepik.android.remote.course.model.EnrollmentRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface EnrollmentService {
    @POST("api/enrollments")
    fun joinCourse(@Body enrollmentCourse: EnrollmentRequest): Completable

    @DELETE("api/enrollments/{id}")
    fun dropCourse(@Path("id") courseId: Long): Completable
}