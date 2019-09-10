package org.stepik.android.presentation.comment.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.mutate
import org.stepic.droid.util.plus
import org.stepik.android.domain.comment.interactor.CommentInteractor
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsStateMapper
@Inject
constructor() {
    fun mapCommentDataItemsToRawItems(commentDataItems: PagedList<CommentItem.Data>): List<CommentItem> {
        val items = ArrayList<CommentItem>()

        var parentItem: CommentItem.Data? = null
        for (i in commentDataItems.indices) {
            val item = commentDataItems[i]
            if (!item.comment.replies.isNullOrEmpty()) {
                parentItem = item
            }

            items += item

            if (item.comment.parent == parentItem?.id &&
                parentItem?.id != null &&
                commentDataItems.getOrNull(i + 1)?.comment?.parent != parentItem.id) {

                val replies = parentItem.comment.replies ?: emptyList()
                val index = replies.indexOf(item.id)

                if (index in 0 until replies.size - 1) {
                    items += CommentItem.LoadMoreReplies(parentItem.comment, item.id, replies.size - index)
                }
            }
        }

        return items
    }

    /**
     * stable state -> pagination loading
     */
    fun mapToLoadMoreState(commentsState: CommentsView.CommentsState.Loaded, direction: CommentInteractor.Direction): CommentsView.CommentsState =
        when (direction) {
            CommentInteractor.Direction.UP ->
                commentsState.copy(commentItems = listOf(CommentItem.Placeholder) + commentsState.commentItems)

            CommentInteractor.Direction.DOWN ->
                commentsState.copy(commentItems = commentsState.commentItems + CommentItem.Placeholder)
        }

    /**
     * Pagination loading -> new stable state
     */
    fun mapFromLoadMoreToSuccess(state: CommentsView.State, items: PagedList<CommentItem.Data>, direction: CommentInteractor.Direction): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState
        val rawItems = mapCommentDataItemsToRawItems(items)

        val (newDataItems: PagedList<CommentItem.Data>, newItems) =
            when (direction) {
                CommentInteractor.Direction.UP ->
                    items + commentsState.commentDataItems to rawItems + commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                CommentInteractor.Direction.DOWN ->
                    commentsState.commentDataItems + items to commentsState.commentItems.dropLastWhile(CommentItem.Placeholder::equals) + rawItems
            }

        return state.copy(commentsState = commentsState.copy(commentDataItems = newDataItems, commentItems = newItems))
    }

    /**
     * Pagination loading -> (rollback) -> previous stable state
     */
    fun mapFromLoadMoreToError(state: CommentsView.State, direction: CommentInteractor.Direction): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val newItems =
            when (direction) {
                CommentInteractor.Direction.UP ->
                   state.commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                CommentInteractor.Direction.DOWN ->
                   state.commentsState.commentItems.dropLastWhile(CommentItem.Placeholder::equals)
            }

        return state.copy(commentsState = state.commentsState.copy(commentItems = newItems))
    }

    /**
     * stable state -> replies loading
     */
    fun mapToLoadMoreRepliesState(commentsState: CommentsView.CommentsState.Loaded, loadMoreReplies: CommentItem.LoadMoreReplies): CommentsView.CommentsState =
        commentsState.copy(
            commentItems = commentsState
                .commentItems
                .map { if (it == loadMoreReplies) CommentItem.ReplyPlaceholder(loadMoreReplies.parentComment) else it }
        )

    /**
     * More replies loading -> new stable state
     */
    fun mapFromLoadMoreRepliesToSuccess(state: CommentsView.State, items: PagedList<CommentItem.Data>, loadMoreReplies: CommentItem.LoadMoreReplies): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState

        val rawIndex = commentsState.commentItems.indexOfFirst { (it as? CommentItem.ReplyPlaceholder)?.id == loadMoreReplies.id }
        val index = commentsState.commentDataItems.indexOfFirst { it.id == loadMoreReplies.lastCommentId }

        return if (rawIndex > 0 && index > 0) {
            val commentItems =
                commentsState.commentItems.mutate {
                    removeAt(rawIndex)
                    addAll(rawIndex, items)
                } // todo: handle LoadMoreReplies for next items

            val commentDataItems =
                commentsState.commentDataItems.mutate { addAll(index + 1, items) }

            state.copy(commentsState = commentsState.copy(commentDataItems, commentItems))
        } else {
            state
        }
    }

    /**
     * More replies loading -> (rollback) -> previous stable state
     */
    fun mapFromLoadMoreRepliesToError(state: CommentsView.State, loadMoreReplies: CommentItem.LoadMoreReplies): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState
        val commentItems = commentsState.commentItems

        return state.copy(commentsState = commentsState.copy(
            commentItems = commentItems.map { if ((it as? CommentItem.ReplyPlaceholder)?.id == loadMoreReplies.id) loadMoreReplies else it }
        ))
    }

    /**
     * VOTES
     */
    fun mapToVotePending(commentsState: CommentsView.CommentsState.Loaded, commentDataItem: CommentItem.Data): CommentsView.CommentsState =
        commentsState.copy(
            commentItems = commentsState
                .commentItems
                .map { if (it == commentDataItem) commentDataItem.copy(voteStatus = CommentItem.Data.VoteStatus.Pending) else it }
        )

    fun mapFromVotePendingToResolved(state: CommentsView.State, vote: Vote): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState

        return state.copy(commentsState = commentsState.copy(
            commentItems = commentsState.commentItems
                .map { item ->
                    if (item is CommentItem.Data &&
                        item.comment.vote == vote.id) {
                        item.copy(voteStatus = CommentItem.Data.VoteStatus.Resolved(vote))
                    } else {
                        item
                    }
                },

            commentDataItems = commentsState.commentDataItems
                .mapPaged { item ->
                    if (item.comment.vote == vote.id) {
                        item.copy(voteStatus = CommentItem.Data.VoteStatus.Resolved(vote))
                    } else {
                        item
                    }
                }
        ))
    }
}