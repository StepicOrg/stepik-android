package org.stepik.android.remote.discussion_proxy

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse
import org.stepik.android.remote.discussion_proxy.service.DiscussionProxyService
import javax.inject.Inject

class DiscussionProxyRemoteDataSourceImpl
@Inject
constructor(
    private val discussionProxyService: DiscussionProxyService
) : DiscussionProxyRemoteDataSource {
    private val discussionProxyResponseMapper =
        Function<DiscussionProxyResponse, List<DiscussionProxy>>(DiscussionProxyResponse::discussionProxies)

    override fun getDiscussionProxies(vararg discussionProxyIds: String): Single<List<DiscussionProxy>> =
        discussionProxyIds
            .chunkedSingleMap {ids ->
                discussionProxyService.getDiscussionProxies(ids)
                    .map(discussionProxyResponseMapper)
            }
}