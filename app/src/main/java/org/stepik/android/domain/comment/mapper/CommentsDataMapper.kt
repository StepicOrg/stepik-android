package org.stepik.android.domain.comment.mapper

import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.latex.mapper.LatexTextMapper
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsDataMapper
@Inject
constructor(
    private val latexTextMapper: LatexTextMapper
) {
    fun mapToCommentDataItems(
        commentIds: LongArray,
        commentsData: CommentsData,
        discussionId: Long? = null,
        cachedCommentItems: List<CommentItem.Data> = emptyList()
    ): List<CommentItem.Data> =
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

                val solution = comment
                    .submission
                    ?.let { submissionId ->
                        commentsData
                            .submissions
                            .find { it.id == submissionId }
                    }
                    ?.let { submission ->
                        commentsData
                            .attempts
                            .find { it.id == submission.attempt }
                            ?.let { attempt ->
                                CommentItem.Data.Solution(attempt, submission)
                            }
                    }

                val latexData = latexTextMapper.mapToLatexText(comment.text ?: "")
                val result = if (latexData is LatexData.Web) {
                    latexData
                } else {
                    latexTextMapper.mapToLatexText(comment.text?.replace("\n", "<br>") ?: "")
                }

                CommentItem.Data(
                    comment = comment,
                    textData = result,
                    user = user,
                    voteStatus = CommentItem.Data.VoteStatus.Resolved(vote),
                    isFocused = discussionId == comment.id,
                    solution = solution
                )
            }
            .let { newItems ->
                val items = newItems + cachedCommentItems

                val comments = items
                    .associateBy { it.comment.id }

                commentIds
                    .flatMap { commentId ->
                        val comment = comments[commentId]

                        if (comment != null) {
                            listOf(comment) + (comment.comment.replies?.mapNotNull(comments::get) ?: emptyList())
                        } else {
                            emptyList()
                        }
                    }
            }
}