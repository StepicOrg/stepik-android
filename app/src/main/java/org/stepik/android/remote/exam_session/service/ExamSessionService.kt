package org.stepik.android.remote.exam_session.service

import io.reactivex.Single
import org.stepik.android.remote.exam_session.model.ExamSessionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ExamSessionService {
    @GET("api/exam-sessions/{id}")
    fun getExamSession(@Path("id") id: Long): Single<ExamSessionResponse>
}