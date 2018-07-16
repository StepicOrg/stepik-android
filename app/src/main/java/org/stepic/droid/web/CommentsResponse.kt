package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepik.android.model.User
import org.stepic.droid.model.comments.Comment
import org.stepic.droid.model.comments.Vote

data class CommentsResponse(
        val detail: String?, // "You do not have permission to perform this action.", null if OK
        val target: List<String>?, // ["Invalid pk '10205111' - object does not exist."], null if OK
        val meta: Meta?, // not null, if OK
        val comments: List<Comment>?,
        val users: List<User>?,
        val votes: List<Vote>?
)