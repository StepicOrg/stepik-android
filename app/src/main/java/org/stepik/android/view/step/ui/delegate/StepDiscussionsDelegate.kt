package org.stepik.android.view.step.ui.delegate

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_step.view.*
import kotlinx.android.synthetic.main.view_step_discussion.view.*
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.comment.model.DiscussionThreadContainer

class StepDiscussionsDelegate(
    containerView: View,
    onDiscussionThreadClicked: (discussionThread: DiscussionThread) -> Unit
) {
    private val delegates =
        mapOf(
            DiscussionThread.THREAD_DEFAULT to Delegate(containerView.stepDiscussions, onDiscussionThreadClicked),
            DiscussionThread.THREAD_SOLUTIONS to Delegate(containerView.stepSolutions, onDiscussionThreadClicked)
        )

    fun setDiscussionThreads(discussionThreads: List<DiscussionThread>) {
        delegates.entries.forEach { (thread, delegate) ->
            delegate.setDiscussionThread(discussionThreads.find { it.thread == thread })
        }
    }

    private class Delegate(
        private val containerView: View,
        onDiscussionThreadClicked: (discussionThread: DiscussionThread) -> Unit
    ) {
        private val stepDiscussions = containerView.stepDiscussionsCount
        private var discussionThread: DiscussionThread? = null

        init {
            containerView.isVisible = false
            containerView.setOnClickListener { discussionThread?.let(onDiscussionThreadClicked) }
        }

        fun setDiscussionThread(discussionThread: DiscussionThread?) {
            this.discussionThread = discussionThread

            val discussionProxy = discussionThread?.discussionProxy
            val discussionsCount = discussionThread?.discussionsCount ?: 0

            when (discussionThread?.thread) {
                DiscussionThread.THREAD_DEFAULT ->
                    setDiscussionThreadData(discussionProxy, discussionsCount, DiscussionThreadContainer.DEFAULT)

                DiscussionThread.THREAD_SOLUTIONS ->
                    setDiscussionThreadData(discussionProxy, discussionsCount, DiscussionThreadContainer.SOLUTIONS)

                else ->
                    containerView.isVisible = false
            }
        }

        private fun setDiscussionThreadData(discussionProxy: String?, discussionsCount: Int, discussionThreadContainer: DiscussionThreadContainer) {
            stepDiscussions.text =
                when {
                    discussionProxy == null ->
                        containerView.context.getString(discussionThreadContainer.disabledStringRes)

                    discussionsCount > 0 ->
                        containerView.context.getString(discussionThreadContainer.showStringRes, discussionsCount)

                    else ->
                        containerView.context.getString(discussionThreadContainer.writeFirstStringRes)
                }
            stepDiscussions.setCompoundDrawables(start = if (discussionProxy != null) discussionThreadContainer.containerDrawable else -1)
            containerView.isEnabled = discussionProxy != null
            containerView.isVisible = true
        }
    }
}