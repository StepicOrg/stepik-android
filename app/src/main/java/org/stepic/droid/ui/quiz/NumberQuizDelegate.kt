package org.stepic.droid.ui.quiz

import android.text.InputType
import android.view.View
import org.stepik.android.model.learning.Submission
import org.stepik.android.model.learning.Reply

class NumberQuizDelegate: StringQuizDelegate() {
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        answerField.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
    }

    override fun setSubmission(submission: Submission?) {
        submission?.reply?.number?.let { answerField.setText(it) }
    }

    override fun createReply() = Reply(number = answerField.text.toString())
}