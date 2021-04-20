package org.stepik.android.remote.exam_session.service

import io.reactivex.Single
import org.stepik.android.remote.exam_session.model.ExamSessionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ExamSessionService {
    @GET("api/exam-sessions")
    fun getExamSessions(@Query("ids[]") ids: List<Long>): Single<ExamSessionResponse>
}