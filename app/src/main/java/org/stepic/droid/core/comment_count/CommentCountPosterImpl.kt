package org.stepic.droid.core.comment_count

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.comment_count.contract.CommentCountListener
import org.stepic.droid.core.comment_count.contract.CommentCountPoster
import org.stepic.droid.model.comments.Comment
import javax.inject.Inject

class CommentCountPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<CommentCountListener>)
    : CommentCountPoster {
    override fun updateCommentCount() {
        listenerContainer
                .asIterable()
                .forEach { it.onCommentCountUpdated() }
    }

}
