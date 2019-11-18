package org.stepik.android.domain.discussion_proxy.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder
import org.stepik.android.domain.discussion_proxy.repository.DiscussionProxyRepository
import org.stepik.android.model.comments.DiscussionProxy
import javax.inject.Inject

class DiscussionProxyInteractor
@Inject
constructor(
    private val discussionProxyRepository: DiscussionProxyRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun getDiscussionProxy(discussionProxyId: String): Single<DiscussionProxy> =
        discussionProxyRepository
            .getDiscussionProxy(discussionProxyId)
            .toSingle()

    fun getDiscussionOrder(): Single<DiscussionOrder> =
        Single.fromCallable {
            sharedPreferenceHelper.discussionOrder
        }

    fun saveDiscussionOrder(discussionOrder: DiscussionOrder): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.discussionOrder = discussionOrder
        }
}