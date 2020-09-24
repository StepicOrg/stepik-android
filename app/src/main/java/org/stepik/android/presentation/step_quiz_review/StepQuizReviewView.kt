package org.stepik.android.presentation.step_quiz_review

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.model.Progress
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz.StepQuizView

interface StepQuizReviewView {
    sealed class State {
        object Idle : State()
        data class Loading(val step: Step) : State()
        object Error : State()

        data class SubmissionNotMade(val quizState: StepQuizView.State, val instruction: ReviewInstruction) : State() // 1
        data class SubmissionNotSelected(val quizState: StepQuizView.State.AttemptLoaded, val instruction: ReviewInstruction) : State() // 2
        data class SubmissionSelectedLoading(val quizState: StepQuizView.State.AttemptLoaded, val instruction: ReviewInstruction) : State() // 2
        data class SubmissionSelected(val quizState: StepQuizView.State.AttemptLoaded, val session: ReviewSession, val instruction: ReviewInstruction) : State() // 3
        data class Completed(val quizState: StepQuizView.State.AttemptLoaded, val progress: Progress) : State() // 3 / 5
    }

    sealed class Message {
        /**
         * Initialization
         */
        data class InitWithStep(val step: Step, val forceUpdate: Boolean = false) : Message()
        data class FetchReviewSessionSuccess(
            val sessionData: ReviewSessionData,
            val instruction: ReviewInstruction,
            val progress: Progress? // assignment progress
        ) : Message()
        data class FetchStepQuizStateSuccess(
            val quizState: StepQuizView.State,
            val instruction: ReviewInstruction
        ) : Message()
        object InitialFetchError : Message()

        /**
         * Submission creation or changing
         */
        object SolveAgain : Message() // solve again
        data class ChangeSubmission(val submission: Submission, val attempt: Attempt) : Message() // change solution from existing

        /**
         * Submitting submission to review
         */
        object CreateSessionWithCurrentSubmission : Message() // selects current solution
        object CreateSessionError : Message() // error during solution selecting
        data class SessionCreated(val reviewSession: ReviewSession) : Message() // solution selected and session created
    }

    sealed class Action {
        data class FetchStepQuizState(val step: Step) : Action() // if there is no review session
        data class FetchReviewSession(val instructionId: Long, val sessionId: Long) : Action()
        data class CreateSessionWithSubmission(val submissionId: Long) : Action() // select solution

        sealed class ViewAction : Action() {
            object ShowNetworkError : ViewAction() // error
        }
    }

    fun render(state: State)
    fun onAction(action: Action.ViewAction)
}