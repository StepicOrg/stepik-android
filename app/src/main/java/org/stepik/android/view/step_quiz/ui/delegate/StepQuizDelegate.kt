package org.stepik.android.view.step_quiz.ui.delegate

import android.view.View
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz_text.TextStepQuizView
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState
import timber.log.Timber

class StepQuizDelegate(
    private val stepQuizFormDelegate: StepQuizFormDelegate,
    private val stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate,
    private val submitButton: View,
    private val onSubmitReply: (Reply) -> Unit
) {
    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()
    private val stepQuizFormMapper = StepQuizFormMapper()

    init {
        submitButton.setOnClickListener { trySubmitReply() }
    }

    private fun trySubmitReply() {
        val validation = stepQuizFormDelegate.validateForm()
        if (validation == null) {
            onSubmitReply(stepQuizFormDelegate.createReply())
        } else {
            stepQuizFeedbackBlocksDelegate.setState(StepQuizFeedbackState.Validation(validation))
        }
    }

    fun setState(state: TextStepQuizView.State) {
        stepQuizFeedbackBlocksDelegate.setState(stepQuizFeedbackMapper.mapToStepQuizFeedbackState(state))
        stepQuizFormDelegate.setState(state)

        Timber.d(state.toString())

        submitButton.isEnabled = stepQuizFormMapper.isQuizSubmitEnabled(state)
    }
}