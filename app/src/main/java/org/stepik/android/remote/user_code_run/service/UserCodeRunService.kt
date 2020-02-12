package org.stepik.android.remote.user_code_run.service

import io.reactivex.Single
import org.stepik.android.remote.user_code_run.model.UserCodeRunRequest
import org.stepik.android.remote.user_code_run.model.UserCodeRunResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserCodeRunService {
    @POST("api/user-code-runs")
    fun createUserCodeRun(
        @Body userCodeRunRequest: UserCodeRunRequest
    ): Single<UserCodeRunResponse>

    @GET("api/user-code-runs/{userCodeRunId}")
    fun getUserCodeRuns(@Path("userCodeRunId") userCodeRunId: Long): Single<UserCodeRunResponse>
}