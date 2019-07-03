package org.stepik.android.view.step_quiz.ui.delegate

import android.widget.TextView
import org.stepic.droid.R
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
    private val context = submitButton.context

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
            submitButton.text = resolveQuizActionButtonText(state)
        }
    }

    private fun resolveQuizActionButtonText(state: StepQuizView.State.AttemptLoaded): String =
        with(state.restrictions) {
            val isSubmissionInTerminalState =
                (state.submissionState as? StepQuizView.SubmissionState.Loaded)
                    ?.submission
                    ?.status
                    .let { it == Submission.Status.CORRECT || it == Submission.Status.WRONG }
            
            if (isSubmissionInTerminalState) {
                if (maxSubmissionCount in 0 until submissionCount) {
                    context.getString(R.string.step_quiz_action_button_no_submissions)
                } else {
                    context.getString(R.string.step_quiz_action_button_try_again)
                }
            } else {
                if (maxSubmissionCount > submissionCount) {
                    val submissionsLeft = maxSubmissionCount - submissionCount
                    context.getString(
                        R.string.step_quiz_action_button_submit_with_counter,
                        context.resources.getQuantityString(R.plurals.submissions, submissionsLeft, submissionsLeft)
                    )
                } else {
                    context.getString(R.string.step_quiz_action_button_submit)
                }
            }
        }
}