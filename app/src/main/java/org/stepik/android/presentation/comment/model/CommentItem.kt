package org.stepik.android.presentation.comment.model

import org.stepik.android.model.comments.Comment
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.Identifiable

data class CommentItem(
    val comment: Comment,
    val user: User
) : Identifiable<Long> {
    override val id: Long =
        comment.id
}