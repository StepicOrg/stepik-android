package org.stepik.android.remote.assignment.service

import io.reactivex.Single
import org.stepik.android.remote.assignment.model.AssignmentResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AssignmentService {
    @GET("api/assignments")
    fun getAssignments(@Query("ids[]") assignmentsIds: List<Long>): Single<AssignmentResponse>
}