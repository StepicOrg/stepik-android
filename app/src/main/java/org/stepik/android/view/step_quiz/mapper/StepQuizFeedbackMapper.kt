package org.stepik.android.view.step_quiz.mapper

import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz_text.TextStepQuizView
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizFeedbackMapper {
    fun mapToStepQuizFeedbackState(state: TextStepQuizView.State): StepQuizFeedbackState =
        if (state is TextStepQuizView.State.SubmissionLoaded) {
            when (state.submission.status) {
                Submission.Status.CORRECT ->
                    StepQuizFeedbackState.Correct(state.submission.hint)

                Submission.Status.WRONG ->
                    StepQuizFeedbackState.Wrong(state.submission.hint)

                Submission.Status.EVALUATION ->
                    StepQuizFeedbackState.Evaluation

                else ->
                    StepQuizFeedbackState.Idle
            }
        } else {
            StepQuizFeedbackState.Idle
        }
}