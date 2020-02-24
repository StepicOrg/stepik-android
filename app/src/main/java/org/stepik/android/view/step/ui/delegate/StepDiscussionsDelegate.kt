package org.stepik.android.view.step.ui.delegate

import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_step.view.*
import kotlinx.android.synthetic.main.view_step_discussion.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.model.comments.DiscussionThread

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
                DiscussionThread.THREAD_DEFAULT -> {
                    val containerText =
                        when {
                            discussionProxy == null ->
                                containerView.context.getString(R.string.comment_disabled)

                            discussionsCount > 0 ->
                                containerView.context.getString(R.string.step_discussion_show, discussionsCount)

                            else ->
                                containerView.context.getString(R.string.step_discussion_write_first)
                        }
                    setDiscussionThreadData(containerText, R.drawable.ic_step_discussion, discussionProxy != null)
                }
                DiscussionThread.THREAD_SOLUTIONS -> {
                    val containerText =
                        when {
                            discussionProxy == null ->
                                containerView.context.getString(R.string.solution_disabled)

                            discussionsCount > 0 ->
                                containerView.context.getString(R.string.step_solutions_show, discussionsCount)

                            else ->
                                containerView.context.getString(R.string.step_solutions_write_first)
                        }
                    setDiscussionThreadData(containerText, R.drawable.ic_step_solutions, discussionProxy != null)
                }
                else ->
                    containerView.isVisible = false
            }
        }

        private fun setDiscussionThreadData(containerText: String, @DrawableRes containerDrawableId: Int, isContainerEnabled: Boolean) {
            stepDiscussions.text = containerText
            stepDiscussions.setCompoundDrawables(start = if (isContainerEnabled) containerDrawableId else -1)
            containerView.isEnabled = isContainerEnabled
            containerView.isVisible = true
        }
    }
}