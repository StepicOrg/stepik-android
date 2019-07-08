package org.stepik.android.data.discussion_proxy.source

import io.reactivex.Single
import org.stepik.android.model.comments.DiscussionProxy

interface DiscussionProxyRemoteDataSource {
    fun getDiscussionProxies(vararg discussionProxyIds: String): Single<List<DiscussionProxy>>
}