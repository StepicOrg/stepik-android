package org.stepik.android.remote.progress.service

import io.reactivex.Single
import org.stepik.android.remote.progress.model.ProgressResponse
import retrofit2.http.GET

interface ProgressService {
    @GET("api/progresses")
    fun getProgressesReactive(progresses: Array<String?>): Single<ProgressResponse>
}