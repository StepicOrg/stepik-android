package org.stepik.android.remote.proctor_session.service

import io.reactivex.Single
import org.stepik.android.remote.proctor_session.model.ProctorSessionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProctorSessionService {
    @GET("api/proctor-sessions/{id}")
    fun getProctorSession(@Path("id") id: Long): Single<ProctorSessionResponse>
}