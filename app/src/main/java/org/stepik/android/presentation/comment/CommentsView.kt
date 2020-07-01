package org.stepik.android.presentation.comment

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.presentation.comment.model.CommentItem

interface CommentsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()

        data class DiscussionLoaded(
            val isGuest: Boolean,
            val discussionProxy: DiscussionProxy,
            val discussionOrder: DiscussionOrder,
            val discussionId: Long? = null,
            val commentsState: CommentsState
        ) : State()
    }

    sealed class CommentsState {
        object Loading : CommentsState()
        object EmptyComments : CommentsState()

        data class Loaded(
            val commentDataItems: PagedList<CommentItem.Data>,
            val commentItems: List<CommentItem>
        ) : CommentsState()
    }

    fun setState(state: State)
    fun focusDiscussion(discussionId: Long)
    fun showNetworkError()
    fun showAuthRequired()
    fun showCommentComposeDialog(step: Step, parent: Long? = null, comment: Comment? = null, submission: Submission? = null)
}