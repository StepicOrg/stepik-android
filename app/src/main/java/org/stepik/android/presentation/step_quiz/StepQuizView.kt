package org.stepik.android.presentation.step_quiz

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

interface StepQuizView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class AttemptLoaded(val attempt: Attempt, val submissionState: SubmissionState) : State()

        object NetworkError : State()
    }

    sealed class SubmissionState {
        object Empty : SubmissionState()
        data class Loaded(val submission: Submission) : SubmissionState()
    }

    fun setState(state: State)
    fun showNetworkError()
}