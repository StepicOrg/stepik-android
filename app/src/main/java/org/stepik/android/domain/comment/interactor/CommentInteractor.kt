package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.comment.mapper.CommentsDataMapper
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.model.DiscussionOrder
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CommentInteractor
@Inject
constructor(
    private val commentRepository: CommentRepository,
    private val commentsDataMapper: CommentsDataMapper,
    private val userPreferences: UserPreferences
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
                    .map { commentsDataMapper.mapToCommentDataItems(commentIds.toLongArray(), it, userPreferences.userId, cachedCommentItems) }
                    .map { comments ->
                        PagedList(comments, hasPrev = start > 0, hasNext = end < orderedCommentIds.size)
                    }
            }

    fun getMoreComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        direction: Direction,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> =
        getMore(
            getOrderedCommentIds(discussionProxy, discussionOrder),
            direction,
            lastCommentId
        )

    fun getMoreReplies(
        comment: Comment,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> =
        getMore(
            comment.replies ?: emptyList(),
            Direction.DOWN,
            lastCommentId
        )

    private fun getMore(
        commentIds: List<Long>,
        direction: Direction,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> {
        val index = commentIds
            .indexOf(lastCommentId)

        if (index < 0) {
            throw IndexOutOfBoundsException("There is no such item with id=$lastCommentId in discussion proxy")
        }

        val (start, end) =
            when (direction) {
                Direction.UP ->
                    max(0, index - PAGE_SIZE) to index

                Direction.DOWN ->
                    index + 1 to min(index + PAGE_SIZE, commentIds.size)
            }

        val slicedCommentIds = commentIds
            .subList(start, end)
            .toLongArray()

        return commentRepository
            .getComments(*slicedCommentIds)
            .map { commentsDataMapper.mapToCommentDataItems(slicedCommentIds, it, userPreferences.userId) }
            .map { comments ->
                PagedList(comments, hasPrev = start > 0, hasNext = end < commentIds.size)
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

    enum class Direction {
        UP, DOWN
    }
}