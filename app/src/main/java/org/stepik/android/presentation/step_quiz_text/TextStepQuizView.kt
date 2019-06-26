package org.stepik.android.presentation.step_quiz_text

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

interface TextStepQuizView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class AttemptLoaded(val attempt: Attempt) : State()
        data class SubmissionLoaded(val attempt: Attempt, val submission: Submission) : State()

        object NetworkError : State()
    }

    fun setState(state: State)
}