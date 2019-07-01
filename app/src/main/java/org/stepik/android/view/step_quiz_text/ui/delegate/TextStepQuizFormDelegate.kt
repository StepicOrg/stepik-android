package org.stepik.android.view.step_quiz_text.ui.delegate

import android.widget.TextView
import org.stepic.droid.R
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz_text.TextStepQuizView
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class TextStepQuizFormDelegate(
    private val textField: TextView
) : StepQuizFormDelegate {
    private val stepQuizFormMapper = StepQuizFormMapper()

    override fun createReply(): Reply =
        Reply(text = textField.text.toString())

    override fun validateForm(): String? =
        if (textField.text.isEmpty()) {
            textField.context.getString(R.string.empty_courses_anonymous) // todo add string res
        } else {
            null
        }

    override fun setState(state: TextStepQuizView.State) {
        if (state !is TextStepQuizView.State.AttemptLoaded) return

        textField.isEnabled = stepQuizFormMapper.isQuizEnabled(state)
        textField.text = (state.submissionState as? TextStepQuizView.SubmissionState.Loaded)
            ?.submission
            ?.reply
            ?.text
            ?: ""
    }
}