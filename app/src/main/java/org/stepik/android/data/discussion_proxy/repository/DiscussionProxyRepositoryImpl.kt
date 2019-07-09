package org.stepik.android.data.discussion_proxy.repository

import io.reactivex.Single
import org.stepik.android.data.discussion_proxy.source.DiscussionProxyRemoteDataSource
import org.stepik.android.domain.discussion_proxy.repository.DiscussionProxyRepository
import org.stepik.android.model.comments.DiscussionProxy
import javax.inject.Inject

class DiscussionProxyRepositoryImpl
@Inject
constructor(
    private val discussionProxyRemoteDataSource: DiscussionProxyRemoteDataSource
) : DiscussionProxyRepository {
    override fun getDiscussionProxies(vararg discussionProxyIds: String): Single<List<DiscussionProxy>> =
        discussionProxyRemoteDataSource
            .getDiscussionProxies(*discussionProxyIds)
}