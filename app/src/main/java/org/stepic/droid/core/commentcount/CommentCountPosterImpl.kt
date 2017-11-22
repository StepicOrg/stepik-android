package org.stepic.droid.core.commentcount

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.commentcount.contract.CommentCountListener
import org.stepic.droid.core.commentcount.contract.CommentCountPoster
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
