package org.stepik.android.presentation.comment

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.domain.comment.interactor.ComposeCommentInteractor
import org.stepik.android.domain.comment.model.DiscussionOrder
import org.stepik.android.domain.discussion_proxy.interactor.DiscussionProxyInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CommentsPresenter
@Inject
constructor(
    private val commentInteractor: CommentInteractor,
    private val composeCommentInteractor: ComposeCommentInteractor,
    private val discussionProxyInteractor: DiscussionProxyInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CommentsView>() {
    private var state: CommentsView.State = CommentsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    /**
     * Data initialization variants
     */
    fun onDiscussion(discussionProxyId: String, discussionId: Long?, forceUpdate: Boolean = false) {
        if (state != CommentsView.State.Idle &&
            !((state == CommentsView.State.NetworkError || state is CommentsView.State.DiscussionLoaded) && forceUpdate)
        ) {
            return
        }

        compositeDisposable.clear()
        state = CommentsView.State.Loading
        compositeDisposable += discussionProxyInteractor
            .getDiscussionProxy(discussionProxyId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CommentsView.State.DiscussionLoaded(it, DiscussionOrder.LAST_DISCUSSION, CommentsView.CommentsState.Loading)
                    fetchComments(discussionId)
                },
                onError = { state = CommentsView.State.NetworkError }
            )
    }

    private fun fetchComments(discussionId: Long?) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        compositeDisposable += commentInteractor
            .getComments(oldState.discussionProxy, discussionId, oldState.discussionOrder)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = oldState.copy(commentsState = CommentsView.CommentsState.Loaded(it)) },
                onError = { state = CommentsView.State.NetworkError }
            )
    }
}