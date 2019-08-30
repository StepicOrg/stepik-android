package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.model.DiscussionOrder
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.presentation.comment.model.CommentItem
import kotlin.math.max
import javax.inject.Inject
import kotlin.math.min

class CommentInteractor
@Inject
constructor(
    private val commentRepository: CommentRepository
) {
    companion object {
        private const val PAGE_SIZE = 10
    }

    fun getComments(vararg commentIds: Long): Single<CommentsData> =
        commentRepository
            .getComments(*commentIds)

    fun getComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        discussionId: Long?,
        cachedCommentItems: List<CommentItem.Data>
    ): Single<PagedList<CommentItem.Data>> =
        Single
            .just(getOrderedCommentIds(discussionProxy, discussionOrder))
            .flatMap { orderedCommentIds ->
                val index = orderedCommentIds.indexOf(discussionId)
                val start = max(index - PAGE_SIZE / 2, 0)
                val end = min(start + PAGE_SIZE, orderedCommentIds.size)

                val commentIds =
                    orderedCommentIds.slice(start until end)

                val cachedComments = cachedCommentItems
                    .filter { it.id in commentIds }

                val cachedCommentIds = cachedComments
                    .map(CommentItem.Data::id)

                commentRepository
                    .getComments(*(commentIds - cachedCommentIds).toLongArray())
                    .map(::mapToCommentItems)
                    .map { commentItems ->
                        (commentItems + cachedComments)
                            .sortedBy { commentIds.indexOf(it.id) }
                    }
                    .map { comments ->
                        PagedList(comments, hasNext = start > 0, hasPrev = end < orderedCommentIds.size)
                    }
            }

    fun getMoreComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        direction: Direction,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> =
        Single
            .just(getOrderedCommentIds(discussionProxy, discussionOrder))
            .flatMap { orderedCommentIds ->
                val index = orderedCommentIds
                    .indexOf(lastCommentId)

                if (index < 0) {
                    throw IndexOutOfBoundsException("There is no such item with id=$lastCommentId in discussion proxy")
                }

                val (start, end) =
                    when (direction) {
                        Direction.UP ->
                            max(0, index - PAGE_SIZE) to index

                        Direction.DOWN ->
                            index to min(index + PAGE_SIZE, orderedCommentIds.size)
                    }

                commentRepository
                    .getComments(*orderedCommentIds.slice(start until end).toLongArray())
                    .map(::mapToCommentItems)
                    .map { comments ->
                        PagedList(comments, hasNext = start > 0, hasPrev = end < orderedCommentIds.size)
                    }
            }

    private fun getOrderedCommentIds(discussionProxy: DiscussionProxy, discussionOrder: DiscussionOrder): List<Long> =
        when (discussionOrder) {
            DiscussionOrder.LAST_DISCUSSION ->
                discussionProxy.discussions

            DiscussionOrder.MOST_LIKED ->
                discussionProxy.discussionsMostLiked

            DiscussionOrder.MOST_ACTIVE ->
                discussionProxy.discussionsMostActive

            DiscussionOrder.RECENT_ACTIVITY ->
                discussionProxy.discussionsRecentActivity
        }

    private fun mapToCommentItems(commentsData: CommentsData): List<CommentItem.Data> =
        commentsData
            .comments
            .mapNotNull { comment ->
                val user = commentsData
                    .users
                    .find { it.id == comment.user }
                    ?: return@mapNotNull null

                val vote = commentsData
                    .votes
                    .find { it.id == comment.vote }
                    ?: return@mapNotNull null

                CommentItem.Data(
                    comment = comment,
                    user = user,
                    voteStatus = CommentItem.Data.VoteStatus.Resolved(vote)
                )
            }

    enum class Direction {
        UP, DOWN
    }
}