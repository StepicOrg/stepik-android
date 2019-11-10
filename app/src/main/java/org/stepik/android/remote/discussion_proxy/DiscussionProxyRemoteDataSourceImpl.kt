package org.stepik.android.remote.discussion_proxy

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse
import org.stepik.android.remote.discussion_proxy.service.DiscussionProxyService
import javax.inject.Inject

class DiscussionProxyRemoteDataSourceImpl
@Inject
constructor(
    private val discussionProxyService: DiscussionProxyService
) : DiscussionProxyRemoteDataSource {
    private val discussionProxyResponseMapper = Function(DiscussionProxyResponse::discussionProxies)

    @Suppress("UNCHECKED_CAST") // discussionProxyIds to Array<String> interop not working
    override fun getDiscussionProxies(vararg discussionProxyIds: String): Single<List<DiscussionProxy>> =
        discussionProxyService
            .getDiscussionProxies(discussionProxyIds as Array<String>)
            .map(discussionProxyResponseMapper)
}