package org.stepik.android.domain.discussion_proxy.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.discussion_proxy.repository.DiscussionProxyRepository
import org.stepik.android.model.comments.DiscussionProxy
import javax.inject.Inject

class DiscussionProxyInteractor
@Inject
constructor(
    private val discussionProxyRepository: DiscussionProxyRepository
) {
    fun getDiscussionProxy(discussionProxyId: String): Single<DiscussionProxy> =
        discussionProxyRepository
            .getDiscussionProxy(discussionProxyId)
            .toSingle()
}