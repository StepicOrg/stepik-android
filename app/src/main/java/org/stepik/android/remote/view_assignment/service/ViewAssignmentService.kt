package org.stepik.android.remote.view_assignment.service

import io.reactivex.Completable
import org.stepik.android.remote.view_assignment.model.ViewAssignmentRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ViewAssignmentService {
    @Headers("Content-Type:application/json")
    @POST("api/views")
    fun postViewed(@Body stepAssignment: ViewAssignmentRequest): Completable
}