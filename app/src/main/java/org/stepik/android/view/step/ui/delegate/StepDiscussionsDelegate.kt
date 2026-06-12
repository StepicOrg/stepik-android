package org.stepik.android.view.step.ui.delegate

import androidx.core.view.isVisible
import org.stepic.droid.databinding.ViewStepDiscussionBinding
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.comment.model.DiscussionThreadContainer

class StepDiscussionsDelegate(
    discussionBinding: ViewStepDiscussionBinding,
    stepSolutionsBinding: ViewStepDiscussionBinding,
    onDiscussionThreadClicked: (discussionThread: DiscussionThread) -> Unit
) {

    private val delegates =
        mapOf(
            DiscussionThread.THREAD_DEFAULT to Delegate(discussionBinding, onDiscussionThreadClicked),
            DiscussionThread.THREAD_SOLUTIONS to Delegate(stepSolutionsBinding, onDiscussionThreadClicked)
        )

    fun setDiscussionThreads(discussionThreads: List<DiscussionThread>) {
        delegates.entries.forEach { (thread, delegate) ->
            delegate.setDiscussionThread(discussionThreads.find { it.thread == thread })
        }
    }

    private class Delegate(
        private val discussionBinding: ViewStepDiscussionBinding,
        onDiscussionThreadClicked: (discussionThread: DiscussionThread) -> Unit
    ) {
        private val stepDiscussions = discussionBinding.stepDiscussionsCount
        private var discussionThread: DiscussionThread? = null

        init {
            discussionBinding.root.isVisible = false
            stepDiscussions.setOnClickListener { discussionThread?.let(onDiscussionThreadClicked) }
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
                    discussionBinding.root.isVisible = false
            }
        }

        private fun setDiscussionThreadData(discussionProxy: String?, discussionsCount: Int, discussionThreadContainer: DiscussionThreadContainer) {
            stepDiscussions.text =
                when {
                    discussionProxy == null ->
                        discussionBinding.root.context.getString(discussionThreadContainer.disabledStringRes)

                    discussionsCount > 0 ->
                        discussionBinding.root.context.getString(discussionThreadContainer.showStringRes, discussionsCount)

                    else ->
                        discussionBinding.root.context.getString(discussionThreadContainer.writeFirstStringRes)
                }
            stepDiscussions.setIconResource(if (discussionProxy != null) discussionThreadContainer.containerDrawable else -1)
            stepDiscussions.isEnabled = discussionProxy != null
            discussionBinding.root.isVisible = true
        }
    }
}
