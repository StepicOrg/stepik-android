package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
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
        discussionId: Long?,
        discussionOrder: DiscussionOrder
    ): Single<List<CommentItem>> =
        Single
            .fromCallable {
                val orderedCommentsId =
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

                val index = orderedCommentsId.indexOf(discussionId)
                val start = max(index - PAGE_SIZE / 2, 0)
                val end = min(start + PAGE_SIZE, orderedCommentsId.size)

                orderedCommentsId.slice(start until end).toLongArray()
            }
            .flatMap { commentRepository.getComments(*it) }
            .map { commentsData ->
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
            }
}