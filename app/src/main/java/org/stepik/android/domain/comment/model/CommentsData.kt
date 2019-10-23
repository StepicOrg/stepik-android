package org.stepik.android.domain.comment.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.model.user.User

data class CommentsData(
    val comments: List<Comment> = emptyList(),
    val users: List<User> = emptyList(),
    val votes: List<Vote> = emptyList(),
    val attempts: List<Attempt> = emptyList(),
    val submissions: List<Submission> = emptyList()
)