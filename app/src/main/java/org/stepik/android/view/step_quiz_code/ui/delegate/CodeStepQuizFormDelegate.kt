package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class CodeStepQuizFormDelegate(
    containerView: View
) : StepQuizFormDelegate {
    private val codeLayout = containerView.codeStepLayout

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(code = codeLayout.text.toString(), language = codeLayout.lang))

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission

        val reply = submission?.reply

        codeLayout.isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayout.setText(reply?.code)
        codeLayout.lang = reply?.language ?: ""
    }
}