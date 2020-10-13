package org.stepik.android.remote.progress.service

import io.reactivex.Single
import org.stepik.android.remote.progress.model.ProgressResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProgressService {
    @GET("api/progresses")
    fun getProgresses(@Query("ids[]") progresses: List<String>): Single<ProgressResponse>
}