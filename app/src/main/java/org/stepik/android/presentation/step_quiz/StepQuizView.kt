package org.stepik.android.presentation.step_quiz

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions

interface StepQuizView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class AttemptLoaded(
            val attempt: Attempt,
            val submissionState: SubmissionState,
            val restrictions: StepQuizRestrictions
        ) : State()

        object NetworkError : State()
    }

    sealed class SubmissionState {
        object Empty : SubmissionState()
        data class Loaded(val submission: Submission) : SubmissionState()
    }

    fun setState(state: State)
    fun showNetworkError()
}