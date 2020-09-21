package org.stepik.android.remote.discussion_proxy.service

import io.reactivex.Single
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DiscussionProxyService {
    @GET("api/discussion-proxies")
    fun getDiscussionProxies(@Query("ids[]") ids: List<String>): Single<DiscussionProxyResponse>
}