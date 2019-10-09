package org.stepik.android.view.step.ui.delegate

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_step_discussion.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables

class StepDiscussionsDelegate(
    private val containerView: View,
    onDiscussionClicked: () -> Unit
) {
    private val stepDiscussions = containerView.stepDiscussionsCount

    init {
        containerView.isVisible = false
        containerView.setOnClickListener { onDiscussionClicked() }

        stepDiscussions.setCompoundDrawables(start = R.drawable.ic_step_discussion)
    }

    fun setDiscussions(discussionProxy: String?, discussionsCount: Int) {
        stepDiscussions.text =
            when {
                discussionProxy == null ->
                    containerView.context.getString(R.string.comment_disabled)

                discussionsCount > 0 ->
                    containerView.context.getString(R.string.step_discussion_show, discussionsCount)

                else ->
                    containerView.context.getString(R.string.step_discussion_write_first)
            }

        stepDiscussions
            .setCompoundDrawables(start = if (discussionProxy != null) R.drawable.ic_step_discussion else -1)

        containerView.isEnabled = discussionProxy != null
        containerView.isVisible = true
    }
}