package org.stepik.android.presentation.comment

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.concatWithPagedList
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.domain.comment.interactor.ComposeCommentInteractor
import org.stepik.android.domain.comment.model.DiscussionOrder
import org.stepik.android.domain.discussion_proxy.interactor.DiscussionProxyInteractor
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.comment.mapper.CommentsStateMapper
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsPresenter
@Inject
constructor(
    private val commentInteractor: CommentInteractor,
    private val composeCommentInteractor: ComposeCommentInteractor,
    private val discussionProxyInteractor: DiscussionProxyInteractor,

    private val commentsStateMapper: CommentsStateMapper,

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

    override fun attachView(view: CommentsView) {
        super.attachView(view)
        view.setState(state)
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
                onSuccess = { fetchComments(it, DiscussionOrder.LAST_DISCUSSION, discussionId) },
                onError = { state = CommentsView.State.NetworkError }
            )
    }

    private fun fetchComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        discussionId: Long?,
        keepCachedComments: Boolean = false
    ) {
        if (discussionProxy.discussions.isEmpty()) {
            state = CommentsView.State.DiscussionLoaded(discussionProxy, discussionOrder, discussionId, CommentsView.CommentsState.EmptyComments)
        } else {
            val cachedComments: List<CommentItem.Data> = ((state as? CommentsView.State.DiscussionLoaded)
                ?.commentsState as? CommentsView.CommentsState.Loaded)
                ?.commentDataItems
                ?.takeIf { keepCachedComments }
                ?: emptyList()

            val newState = CommentsView.State.DiscussionLoaded(discussionProxy, discussionOrder, discussionId, CommentsView.CommentsState.Loading)
            state = newState
            compositeDisposable += commentInteractor
                .getComments(discussionProxy, discussionOrder, discussionId, cachedComments)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribeBy(
                    onSuccess = { commentDataItems: PagedList<CommentItem.Data> ->
                        state = newState.copy(commentsState = CommentsView.CommentsState.Loaded(commentDataItems, commentDataItems))
                    },
                    onError = { state = CommentsView.State.NetworkError }
                )
        }
    }

    fun changeDiscussionOrder(discussionOrder: DiscussionOrder) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        fetchComments(oldState.discussionProxy, discussionOrder, oldState.discussionId, keepCachedComments = true)
    }

    fun onLoadMore(direction: CommentInteractor.Direction) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentsState = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?: return

        val commentDataItems =
            commentsState.commentDataItems

        val lastCommentId =
            when (direction) {
                CommentInteractor.Direction.UP ->
                    commentDataItems
                        .takeIf { it.hasPrev }
                        .takeIf { commentsState.commentItems.first() !is CommentItem.Placeholder }
                        ?.first()
                        ?.id
                        ?: return

                CommentInteractor.Direction.DOWN ->
                    commentDataItems
                        .takeIf { it.hasNext }
                        .takeIf { commentsState.commentItems.last() !is CommentItem.Placeholder }
                        ?.last { it.comment.parent == null }
                        ?.id
                        ?: return
            }

        state = oldState.copy(commentsState = commentsStateMapper.mapToLoadMoreState(commentsState, direction))
        compositeDisposable += commentInteractor
            .getMoreComments(oldState.discussionProxy, oldState.discussionOrder, direction, lastCommentId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = commentsStateMapper.mapFromLoadMoreToSuccess(state, it, direction) },
                onError = { state = commentsStateMapper.mapFromLoadMoreToError(state, direction); }
            )
    }

    fun onLoadMoreReplies(loadMoreReplies: CommentItem.LoadMoreReplies) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentDataItems = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?.commentDataItems
            ?: return

        compositeDisposable += commentInteractor
            .getMoreReplies(loadMoreReplies.parentComment, loadMoreReplies.lastCommentId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    val items = commentDataItems.concatWithPagedList(it)
                    state = oldState.copy(commentsState = CommentsView.CommentsState.Loaded(items, items))
                },
                onError = { state = oldState }
            )
    }

}