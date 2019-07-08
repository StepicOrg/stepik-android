package org.stepik.android.remote.discussion_proxy

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse
import javax.inject.Inject

class DiscussionProxyRemoteDataSourceImpl
@Inject
constructor(
    private val loggedService: StepicRestLoggedService
) : DiscussionProxyRemoteDataSource {
    private val discussionProxyResponseMapper = Function(DiscussionProxyResponse::discussionProxies)

    override fun getDiscussionProxies(vararg discussionProxyIds: String): Single<List<DiscussionProxy>> =
        loggedService
            .getDiscussionProxies(discussionProxyIds)
            .map(discussionProxyResponseMapper)
}