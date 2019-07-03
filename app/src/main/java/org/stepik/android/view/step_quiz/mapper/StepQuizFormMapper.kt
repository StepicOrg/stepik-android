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
        state.submissionState.submission.status.let { it == Submission.Status.CORRECT || it == Submission.Status.WRONG } &&
        with(state.restrictions) { maxSubmissionCount < 0 || maxSubmissionCount > submissionCount }
}