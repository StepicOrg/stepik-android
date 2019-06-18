package org.stepik.android.view.step.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.view_step_discussion.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.setCompoundDrawables

class StepDiscussionsDelegate(
    private val containerView: View,
    onDiscussionClicked: () -> Unit
) {
    private val stepDiscussions = containerView.stepDiscussionsCount

    init {
        containerView.changeVisibility(needShow = false)
        containerView.setOnClickListener { onDiscussionClicked() }

        stepDiscussions.setCompoundDrawables(start = R.drawable.ic_step_discussion)
    }

    fun setDiscussionsCount(discussionsCount: Int) {
        stepDiscussions.text =
            if (discussionsCount > 0) {
                containerView.context.getString(R.string.step_discussion_show, discussionsCount)
            } else {
                containerView.context.getString(R.string.step_discussion_write_first)
            }

        containerView.changeVisibility(needShow = true)
    }
}