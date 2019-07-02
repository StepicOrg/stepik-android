package org.stepik.android.view.step_quiz.ui.delegate

import android.support.annotation.StringRes
import android.widget.TextView
import org.stepic.droid.R
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizDelegate(
    private val stepQuizFormDelegate: StepQuizFormDelegate,
    private val stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate,
    private val submitButton: TextView,
    private val presenter: StepQuizPresenter
) {
    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()
    private val stepQuizFormMapper = StepQuizFormMapper()

    init {
        submitButton.setOnClickListener { trySubmitReply() }
    }

    private fun trySubmitReply() {
        val validation = stepQuizFormDelegate.validateForm()
        if (validation == null) {
            presenter.createSubmission(stepQuizFormDelegate.createReply())
        } else {
            stepQuizFeedbackBlocksDelegate.setState(StepQuizFeedbackState.Validation(validation))
        }
    }

    fun setState(state: StepQuizView.State) {
        stepQuizFeedbackBlocksDelegate.setState(stepQuizFeedbackMapper.mapToStepQuizFeedbackState(state))
        stepQuizFormDelegate.setState(state)

        submitButton.isEnabled = stepQuizFormMapper.isQuizSubmitEnabled(state)

        if (state is StepQuizView.State.AttemptLoaded) {
            @StringRes
            val submitButtonTextRes =
                when ((state.submissionState as? StepQuizView.SubmissionState.Loaded)?.submission?.status) {
                    Submission.Status.CORRECT,
                    Submission.Status.WRONG ->
                        R.string.step_quiz_submit_button_try_again

                    else ->
                        R.string.step_quiz_submit_button_action
                }
            submitButton.setText(submitButtonTextRes)
        }
    }
}