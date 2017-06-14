package org.stepic.droid.core.comment_count.contract

import org.stepic.droid.model.comments.Comment

interface CommentCountPoster {
    fun updateCommentCount(target: Long, comment: Comment)
}