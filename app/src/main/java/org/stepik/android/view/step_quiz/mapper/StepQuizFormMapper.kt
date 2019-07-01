package org.stepik.android.view.step_quiz.mapper

import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz_text.TextStepQuizView

class StepQuizFormMapper {
    fun isQuizEnabled(state: TextStepQuizView.State): Boolean =
        state is TextStepQuizView.State.AttemptLoaded &&
        (
                state.submissionState is TextStepQuizView.SubmissionState.Empty ||
                state.submissionState is TextStepQuizView.SubmissionState.Loaded &&
                state.submissionState.submission.status == Submission.Status.LOCAL
        )

    fun isQuizSubmitEnabled(state: TextStepQuizView.State): Boolean =
        isQuizEnabled(state) ||
        state is TextStepQuizView.State.AttemptLoaded &&
        state.submissionState is TextStepQuizView.SubmissionState.Loaded &&
        (state.submissionState.submission.status == Submission.Status.CORRECT || state.submissionState.submission.status == Submission.Status.WRONG)
}