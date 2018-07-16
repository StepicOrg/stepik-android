package org.stepic.droid.ui.fragments

import org.stepik.android.model.learning.replies.Reply

class MathStepFragment : SingleLineSendStepFragment() {

    override fun generateReply(): Reply = Reply(formula = answerField.text.toString())

    override fun onRestoreSubmission() {
        val formula = submission.reply?.formula ?: return
        answerField.setText(formula)
    }

}
