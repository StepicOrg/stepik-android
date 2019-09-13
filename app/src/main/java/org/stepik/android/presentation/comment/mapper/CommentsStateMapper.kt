package org.stepik.android.presentation.comment.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.mutate
import org.stepic.droid.util.plus
import org.stepic.droid.util.filterNot
import org.stepik.android.domain.base.PaginationDirection
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
    fun mapToLoadMoreState(commentsState: CommentsView.CommentsState.Loaded, direction: PaginationDirection): CommentsView.CommentsState =
        when (direction) {
            PaginationDirection.UP ->
                commentsState.copy(commentItems = listOf(CommentItem.Placeholder) + commentsState.commentItems)

            PaginationDirection.DOWN ->
                commentsState.copy(commentItems = commentsState.commentItems + CommentItem.Placeholder)
        }

    /**
     * Pagination loading -> new stable state
     */
    fun mapFromLoadMoreToSuccess(state: CommentsView.State, items: PagedList<CommentItem.Data>, direction: PaginationDirection): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState
        val rawItems = mapCommentDataItemsToRawItems(items)

        val (newDataItems: PagedList<CommentItem.Data>, newItems) =
            when (direction) {
                PaginationDirection.UP ->
                    items + commentsState.commentDataItems to rawItems + commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                PaginationDirection.DOWN ->
                    commentsState.commentDataItems + items to commentsState.commentItems.dropLastWhile(CommentItem.Placeholder::equals) + rawItems
            }

        return state.copy(commentsState = commentsState.copy(commentDataItems = newDataItems, commentItems = newItems))
    }

    /**
     * Pagination loading -> (rollback) -> previous stable state
     */
    fun mapFromLoadMoreToError(state: CommentsView.State, direction: PaginationDirection): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val newItems =
            when (direction) {
                PaginationDirection.UP ->
                   state.commentsState.commentItems.dropWhile(CommentItem.Placeholder::equals)

                PaginationDirection.DOWN ->
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

    fun mapFromVotePendingToSuccess(state: CommentsView.State, commentDataItem: CommentItem.Data): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsState = state.commentsState

        return state.copy(commentsState = commentsState.copy(
            commentItems = commentsState.commentItems
                .map { item ->
                    if (item is CommentItem.Data &&
                        item.id == commentDataItem.id) {
                        commentDataItem
                    } else {
                        item
                    }
                },

            commentDataItems = commentsState.commentDataItems
                .mapPaged { item ->
                    if (item.id == commentDataItem.id) {
                        commentDataItem
                    } else {
                        item
                    }
                }
        ))
    }

    fun mapFromVotePendingToError(state: CommentsView.State, vote: Vote): CommentsView.State {
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

    /**
     * Remove comment
     */
    fun mapToRemovePending(commentsState: CommentsView.CommentsState.Loaded, commentDataItem: CommentItem.Data): CommentsView.CommentsState =
        commentsState.copy(
            commentItems = commentsState
                .commentItems
                .map { if (it == commentDataItem) CommentItem.RemovePlaceholder(commentDataItem.id, isReply = commentDataItem.comment.parent != null) else it }
        )

    fun mapFromRemovePendingToSuccess(state: CommentsView.State, commentId: Long): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        val commentsToRemove = state.commentsState
            .commentDataItems
            .mapNotNull { it.takeIf { it.id == commentId || it.comment.parent == commentId }?.id }
            .plus(commentId)

        return state.copy(
            discussionProxy =
                with(state.discussionProxy) {
                    copy(
                        discussions = discussions - commentsToRemove,
                        discussionsMostActive = discussionsMostActive - commentsToRemove,
                        discussionsMostLiked = discussionsMostLiked - commentsToRemove,
                        discussionsRecentActivity = discussionsRecentActivity - commentsToRemove
                    )
                },
            discussionId = state.discussionId.takeIf { it != commentId },
            commentsState =
                with(state.commentsState) {
                    copy(
                        commentDataItems = commentDataItems.filterNot { it.id in commentsToRemove },
                        commentItems = commentItems.filterNot { it is CommentItem.RemovePlaceholder && it.id == commentId || it is CommentItem.Data && it.id in commentsToRemove }
                    )
                }
        )
    }

    fun mapFromRemovePendingToError(state: CommentsView.State, commentDataItem: CommentItem.Data): CommentsView.State {
        if (state !is CommentsView.State.DiscussionLoaded ||
            state.commentsState !is CommentsView.CommentsState.Loaded) {
            return state
        }

        return state.copy(
            commentsState =
                with(state.commentsState) {
                    copy(
                        commentItems = commentItems
                            .map { if (it is CommentItem.RemovePlaceholder && it.id == commentDataItem.id) commentDataItem else it }
                    )
                }
        )
    }
}