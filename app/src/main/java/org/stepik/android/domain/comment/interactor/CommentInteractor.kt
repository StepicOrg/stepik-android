package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.comment.mapper.CommentsDataMapper
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.domain.discussion_proxy.mapper.getOrdering
import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder
import org.stepik.android.domain.vote.repository.VoteRepository
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CommentInteractor
@Inject
constructor(
    private val commentRepository: CommentRepository,
    private val voteRepository: VoteRepository,

    private val commentsDataMapper: CommentsDataMapper
) {
    companion object {
        private const val PAGE_SIZE = 10
    }

    fun getComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        discussionId: Long?,
        cachedCommentItems: List<CommentItem.Data>
    ): Single<PagedList<CommentItem.Data>> =
        Single
            .just(discussionProxy.getOrdering(discussionOrder))
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
                    .map { commentsDataMapper.mapToCommentDataItems(commentIds.toLongArray(), it, discussionId, cachedCommentItems) }
                    .map { comments ->
                        PagedList(comments, hasPrev = start > 0, hasNext = end < orderedCommentIds.size)
                    }
            }

    fun getMoreComments(
        discussionProxy: DiscussionProxy,
        discussionOrder: DiscussionOrder,
        direction: PaginationDirection,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> =
        getMore(
            discussionProxy.getOrdering(discussionOrder),
            direction,
            lastCommentId
        )

    fun getMoreReplies(
        comment: Comment,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> =
        getMore(
            comment.replies ?: emptyList(),
            PaginationDirection.NEXT,
            lastCommentId
        )

    private fun getMore(
        commentIds: List<Long>,
        direction: PaginationDirection,
        lastCommentId: Long
    ): Single<PagedList<CommentItem.Data>> {
        val index = commentIds
            .indexOf(lastCommentId)

        if (index < 0) {
            throw IndexOutOfBoundsException("There is no such item with id=$lastCommentId in discussion proxy")
        }

        val (start, end) =
            when (direction) {
                PaginationDirection.PREV ->
                    max(0, index - PAGE_SIZE) to index

                PaginationDirection.NEXT ->
                    index + 1 to min(index + PAGE_SIZE, commentIds.size)
            }

        val slicedCommentIds = commentIds
            .subList(start, end)
            .toLongArray()

        return commentRepository
            .getComments(*slicedCommentIds)
            .map { commentsDataMapper.mapToCommentDataItems(slicedCommentIds, it) }
            .map { comments ->
                PagedList(comments, hasPrev = start > 0, hasNext = end < commentIds.size)
            }
    }

    fun changeCommentVote(commentId: Long, vote: Vote): Single<CommentItem.Data> =
        voteRepository
            .saveVote(vote)
            .flatMap {
                commentRepository
                    .getComments(commentId)
                    .map { commentsData ->
                        commentsDataMapper
                            .mapToCommentDataItems(longArrayOf(commentId), commentsData)
                            .first()
                    }
            }

    /**
     * Edit comments
     */
    fun mapCommentsDataToCommentItem(commentsData: CommentsData): CommentItem.Data? =
        commentsDataMapper
            .mapToCommentDataItems(
                commentIds = commentsData.comments.firstOrNull()?.let { longArrayOf(it.id) } ?: longArrayOf(),
                commentsData = commentsData
            )
            .firstOrNull()
}