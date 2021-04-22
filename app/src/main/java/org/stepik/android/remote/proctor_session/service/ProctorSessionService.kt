package org.stepik.android.remote.proctor_session.service

import io.reactivex.Single
import org.stepik.android.remote.proctor_session.model.ProctorSessionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProctorSessionService {
    @GET("api/proctor-sessions")
    fun getProctorSessions(@Query("ids[]") ids: List<Long>): Single<ProctorSessionResponse>
}