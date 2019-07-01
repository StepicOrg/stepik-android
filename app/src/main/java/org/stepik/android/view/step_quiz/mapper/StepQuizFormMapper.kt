package org.stepik.android.view.step_quiz.mapper

import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView

class StepQuizFormMapper {
    fun isQuizEnabled(state: StepQuizView.State): Boolean =
        state is StepQuizView.State.AttemptLoaded &&
        (
                state.submissionState is StepQuizView.SubmissionState.Empty ||
                state.submissionState is StepQuizView.SubmissionState.Loaded &&
                state.submissionState.submission.status == Submission.Status.LOCAL
        )

    fun isQuizSubmitEnabled(state: StepQuizView.State): Boolean =
        isQuizEnabled(state) ||
        state is StepQuizView.State.AttemptLoaded &&
        state.submissionState is StepQuizView.SubmissionState.Loaded &&
        (state.submissionState.submission.status == Submission.Status.CORRECT || state.submissionState.submission.status == Submission.Status.WRONG)
}