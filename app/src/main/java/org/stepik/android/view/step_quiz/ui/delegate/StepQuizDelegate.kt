package org.stepik.android.view.step_quiz.ui.delegate

import android.view.View
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.ui.util.changeVisibility
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizDelegate(
    private val step: Step,
    private val stepQuizFormDelegate: StepQuizFormDelegate,
    private val stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate,

    private val stepQuizActionButton: TextView,
    private val stepRetryButton: View,
    private val stepQuizDiscountingPolicy: TextView,

    private val stepQuizPresenter: StepQuizPresenter,

    private val onNextClicked: () -> Unit
) {
    private val context = stepQuizActionButton.context

    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()

    private var currentState: StepQuizView.State.AttemptLoaded? = null

    init {
        stepQuizActionButton.setOnClickListener { onActionButtonClicked() }
        stepRetryButton.setOnClickListener { stepQuizPresenter.createAttempt(step) }
    }

    private fun onActionButtonClicked() {
        val state = currentState ?: return

        if (StepQuizFormResolver.isSubmissionInTerminalState(state)) {
            if ((state.submissionState as? StepQuizView.SubmissionState.Loaded)?.submission?.status == Submission.Status.CORRECT) {
                onNextClicked()
            } else {
                stepQuizPresenter.createAttempt(step)
            }
        } else {
            when (val replyResult = stepQuizFormDelegate.createReply()) {
                is ReplyResult.Success ->
                    stepQuizPresenter.createSubmission(replyResult.reply)

                is ReplyResult.Error ->
                    stepQuizFeedbackBlocksDelegate.setState(StepQuizFeedbackState.Validation(replyResult.message))
            }
        }
    }

    fun setState(state: StepQuizView.State.AttemptLoaded) {
        currentState = state

        stepQuizFeedbackBlocksDelegate.setState(stepQuizFeedbackMapper.mapToStepQuizFeedbackState(step, state))
        stepQuizFormDelegate.setState(state)

        stepQuizActionButton.isEnabled = StepQuizFormResolver.isQuizActionEnabled(state)
        stepQuizActionButton.text = resolveQuizActionButtonText(state)

        stepRetryButton.changeVisibility(StepQuizFormResolver.isQuizRetryEnabled(state))

        val isNeedShowDiscountingPolicy =
            state.restrictions.discountingPolicyType != DiscountingPolicyType.NoDiscount &&
            (state.submissionState as? StepQuizView.SubmissionState.Loaded)?.submission?.status != Submission.Status.CORRECT

        stepQuizDiscountingPolicy.changeVisibility(isNeedShowDiscountingPolicy)
        stepQuizDiscountingPolicy.text = resolveQuizDiscountingPolicyText(state)
    }

    private fun resolveQuizActionButtonText(state: StepQuizView.State.AttemptLoaded): String =
        with(state.restrictions) {
            if (StepQuizFormResolver.isSubmissionInTerminalState(state)) {
                state.submissionState as StepQuizView.SubmissionState.Loaded

                when {
                    state.submissionState.submission.status == Submission.Status.CORRECT ->
                        context.getString(R.string.next)

                    maxSubmissionCount in 0 until submissionCount ->
                        context.getString(R.string.step_quiz_action_button_no_submissions)

                    else ->
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

    private fun resolveQuizDiscountingPolicyText(state: StepQuizView.State.AttemptLoaded): String? =
        with(state.restrictions) {
            when (discountingPolicyType) {
                DiscountingPolicyType.Inverse ->
                    context.getString(R.string.discount_policy_inverse_title)

                DiscountingPolicyType.FirstOne, DiscountingPolicyType.FirstThree -> {
                    val remainingSubmissionCount = discountingPolicyType.numberOfTries() - state.restrictions.submissionCount
                    if (remainingSubmissionCount > 0) {
                        context.resources.getQuantityString(R.plurals.discount_policy_first_n, remainingSubmissionCount, remainingSubmissionCount)
                    } else {
                        context.getString(R.string.discount_policy_no_way)
                    }
                }

                else ->
                    null
            }
        }

    fun syncReplyState() {
        if (StepQuizFormResolver.isSubmissionInTerminalState(currentState ?: return)) return

        val reply = (stepQuizFormDelegate.createReply() as? ReplyResult.Success)
            ?.reply
            ?: return

        stepQuizPresenter.syncReplyState(reply)
    }
}