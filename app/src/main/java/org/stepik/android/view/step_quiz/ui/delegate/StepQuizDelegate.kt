package org.stepik.android.view.step_quiz.ui.delegate

import android.widget.TextView
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizDelegate(
    private val stepQuizFormDelegate: StepQuizFormDelegate,
    private val stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate,
    private val actionButton: TextView,
    private val stepQuizDiscountingPolicy: TextView,
    private val presenter: StepQuizPresenter
) {
    private val context = actionButton.context

    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()
    private val stepQuizFormMapper = StepQuizFormMapper()

    private var currentState: StepQuizView.State.AttemptLoaded? = null

    init {
        actionButton.setOnClickListener { onActionButtonClicked() }
    }

    private fun onActionButtonClicked() {
        val state = currentState ?: return

        if (stepQuizFormMapper.isSubmissionInTerminalState(state)) {
            presenter.createAttempt(state.attempt.step)
        } else {
            when (val replyResult = stepQuizFormDelegate.createReply()) {
                is ReplyResult.Success ->
                    presenter.createSubmission(replyResult.reply)

                is ReplyResult.Error ->
                    stepQuizFeedbackBlocksDelegate.setState(StepQuizFeedbackState.Validation(replyResult.message))
            }
        }
    }

    fun setState(state: StepQuizView.State.AttemptLoaded) {
        currentState = state

        stepQuizFeedbackBlocksDelegate.setState(stepQuizFeedbackMapper.mapToStepQuizFeedbackState(state))
        stepQuizFormDelegate.setState(state)

        actionButton.isEnabled = stepQuizFormMapper.isQuizActionEnabled(state)
        actionButton.text = resolveQuizActionButtonText(state)
    }

    private fun resolveQuizActionButtonText(state: StepQuizView.State.AttemptLoaded): String =
        with(state.restrictions) {
            if (stepQuizFormMapper.isSubmissionInTerminalState(state)) {
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

    fun syncReplyState() {
        if (stepQuizFormMapper.isSubmissionInTerminalState(currentState ?: return)) return

        val reply = (stepQuizFormDelegate.createReply() as? ReplyResult.Success)
            ?.reply
            ?: return

        presenter.syncReplyState(reply)
    }
}