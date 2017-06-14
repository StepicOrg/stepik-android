package org.stepic.droid.core.comment_count.contract

import org.stepic.droid.model.comments.Comment

interface CommentCountListener {
    fun onCommentCountUpdated (target : Long, comment : Comment)
}
