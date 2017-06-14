package org.stepic.droid.core.comments

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.comments.contract.CommentsListener
import org.stepic.droid.core.comments.contract.CommentsPoster
import javax.inject.Inject

class CommentsPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<CommentsListener>)
    : CommentsPoster {

    override fun connectionProblem() {
        listenerContainer.asIterable().forEach(CommentsListener::onCommentsConnectionProblem)
    }

    override fun commentsLoaded() {
        listenerContainer.asIterable().forEach(CommentsListener::onCommentsLoaded)
    }


}