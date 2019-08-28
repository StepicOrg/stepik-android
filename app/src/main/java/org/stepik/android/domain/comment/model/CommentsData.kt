package org.stepik.android.domain.comment.model

import org.stepic.droid.util.PagedList
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.model.user.User

data class CommentsData(
    val comments: PagedList<Comment>,
    val users: List<User>,
    val votes: List<Vote>
)