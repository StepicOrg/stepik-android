package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetails
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetailsArrow
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetailsContent
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class CodeStepQuizFormDelegate(
    containerView: View,
    private val stepWrapper: StepPersistentWrapper
) : StepQuizFormDelegate {
    private val codeLayout = containerView.codeStepLayout

    private val stepQuizCodeDetails = containerView.stepQuizCodeDetails
    private val stepQuizCodeDetailsArrow = containerView.stepQuizCodeDetailsArrow
    private val stepQuizCodeDetailsContent = containerView.stepQuizCodeDetailsContent

    init {
        stepQuizCodeDetails.setOnClickListener {
            stepQuizCodeDetailsArrow.changeState()
            if (stepQuizCodeDetailsArrow.isExpanded()) {
                StepikAnimUtils.expand(stepQuizCodeDetailsContent)
            } else {
                StepikAnimUtils.collapse(stepQuizCodeDetailsContent)
            }
        }
    }

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