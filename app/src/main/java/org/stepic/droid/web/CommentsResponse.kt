package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepic.droid.model.User
import org.stepic.droid.model.comments.Comment
import org.stepic.droid.model.comments.Vote

data class CommentsResponse(
        val meta: Meta?,
        val comments: List<Comment>,
        val users: List<User>,
        val votes: List<Vote>
)