package org.stepik.android.presentation.comment

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.domain.comment.interactor.ComposeCommentInteractor
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder
import org.stepik.android.domain.discussion_proxy.interactor.DiscussionProxyInteractor
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.comment.mapper.CommentsStateMapper
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.injection.step.StepDiscussionBus
import javax.inject.Inject

class CommentsPresenter
@Inject
constructor(
    private val commentInteractor: CommentInteractor,
    private val composeCommentInteractor: ComposeCommentInteractor,
    private val discussionProxyInteractor: DiscussionProxyInteractor,

    private val commentsStateMapper: CommentsStateMapper,

    @StepDiscussionBus
    private val stepDiscussionSubject: PublishSubject<Long>,

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
                    onSuccess = {
                        state = newState.copy(commentsState = CommentsView.CommentsState.Loaded(it, commentsStateMapper.mapCommentDataItemsToRawItems(it)))
                        if (discussionId != null) {
                            view?.focusDiscussion(discussionId)
                        }
                    },
                    onError = { state = CommentsView.State.NetworkError }
                )
        }
    }

    /**
     * Discussion ordering
     */
    fun changeDiscussionOrder(discussionOrder: DiscussionOrder) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        fetchComments(oldState.discussionProxy, discussionOrder, oldState.discussionId, keepCachedComments = true)
    }

    /**
     * Load more logic in both directions
     */
    fun onLoadMore(direction: PaginationDirection) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentsState = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?: return

        val commentDataItems =
            commentsState.commentDataItems

        val lastCommentId =
            when (direction) {
                PaginationDirection.UP ->
                    commentDataItems
                        .takeIf { it.hasPrev }
                        .takeIf { commentsState.commentItems.first() !is CommentItem.Placeholder }
                        ?.first()
                        ?.id
                        ?: return

                PaginationDirection.DOWN ->
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
                onError = { state = commentsStateMapper.mapFromLoadMoreToError(state, direction); view?.showNetworkError() }
            )
    }

    /**
     * Load more comments logic
     */
    fun onLoadMoreReplies(loadMoreReplies: CommentItem.LoadMoreReplies) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentsState = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?: return

        state = oldState.copy(commentsState = commentsStateMapper.mapToLoadMoreRepliesState(commentsState, loadMoreReplies))
        compositeDisposable += commentInteractor
            .getMoreReplies(loadMoreReplies.parentComment, loadMoreReplies.lastCommentId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = commentsStateMapper.mapFromLoadMoreRepliesToSuccess(state, it, loadMoreReplies) },
                onError = { state = commentsStateMapper.mapFromLoadMoreRepliesToError(state, loadMoreReplies); view?.showNetworkError() }
            )
    }

    /**
     * Vote logic
     *
     * if [voteValue] is equal to current value new value will be null
     */
    fun onChangeVote(commentDataItem: CommentItem.Data, voteValue: Vote.Value) {
        if (commentDataItem.voteStatus !is CommentItem.Data.VoteStatus.Resolved ||
            commentDataItem.comment.actions?.vote != true) {
            return
        }

        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentsState = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?: return

        val newVote = commentDataItem.voteStatus.vote
            .copy(value = voteValue.takeIf { it != commentDataItem.voteStatus.vote.value })

        state = oldState.copy(commentsState = commentsStateMapper.mapToVotePending(commentsState, commentDataItem))
        compositeDisposable += commentInteractor
            .changeCommentVote(commentDataItem.id, newVote)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = { state = commentsStateMapper.mapFromVotePendingToSuccess(state, it) },
                onError = { state = commentsStateMapper.mapFromVotePendingToError(state, commentDataItem.voteStatus.vote); view?.showNetworkError() }
            )
    }

    /**
     * Edit logic
     */
    fun onCommentCreated(commentsData: CommentsData) {
        val commentDataItem = commentInteractor
            .mapCommentsDataToCommentItem(commentsData)
            ?: return

        state =
            if (commentDataItem.comment.parent != null) {
                commentsStateMapper.insertCommentReply(state, commentDataItem)
            } else {
                stepDiscussionSubject.onNext(commentDataItem.comment.parent ?: -1)
                commentsStateMapper.insertComment(state, commentDataItem)
            }
        view?.focusDiscussion(commentDataItem.id)
    }

    fun onCommentUpdated(commentsData: CommentsData) {
        val commentDataItem = commentInteractor
            .mapCommentsDataToCommentItem(commentsData)
            ?: return

        state = commentsStateMapper.mapFromVotePendingToSuccess(state, commentDataItem)
    }

    fun removeComment(commentId: Long) {
        val oldState = (state as? CommentsView.State.DiscussionLoaded)
            ?: return

        val commentsState = (oldState.commentsState as? CommentsView.CommentsState.Loaded)
            ?: return

        val commentDataItem = commentsState.commentDataItems.find { it.id == commentId }
            ?: return

        state = oldState.copy(commentsState = commentsStateMapper.mapToRemovePending(commentsState, commentDataItem))
        compositeDisposable += composeCommentInteractor
            .removeComment(commentDataItem.id)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = commentsStateMapper.mapFromRemovePendingToSuccess(state, commentId) },
                onError = { state = commentsStateMapper.mapFromRemovePendingToError(state, commentDataItem); view?.showNetworkError() }
            )
    }
}