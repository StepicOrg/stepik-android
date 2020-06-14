package org.stepik.android.remote.discussion_thread.service

import io.reactivex.Single
import org.stepik.android.remote.discussion_thread.model.DiscussionThreadResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DiscussionThreadService {
    @GET("api/discussion-threads")
    fun getDiscussionThreads(@Query("ids[]") ids: Array<String>): Single<DiscussionThreadResponse>
}