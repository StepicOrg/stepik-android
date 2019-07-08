package org.stepik.android.view.step_quiz.mapper

import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizFeedbackMapper {
    fun mapToStepQuizFeedbackState(state: StepQuizView.State): StepQuizFeedbackState =
        if (state is StepQuizView.State.AttemptLoaded && state.submissionState is StepQuizView.SubmissionState.Loaded) {
            when (state.submissionState.submission.status) {
                Submission.Status.CORRECT ->
                    StepQuizFeedbackState.Correct(state.submissionState.submission.hint?.takeIf(String::isNotEmpty))

                Submission.Status.WRONG ->
                    StepQuizFeedbackState.Wrong(state.submissionState.submission.hint?.takeIf(String::isNotEmpty))

                Submission.Status.EVALUATION ->
                    StepQuizFeedbackState.Evaluation

                else ->
                    StepQuizFeedbackState.Idle
            }
        } else {
            StepQuizFeedbackState.Idle
        }
}