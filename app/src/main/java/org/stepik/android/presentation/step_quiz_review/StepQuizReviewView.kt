package org.stepik.android.presentation.step_quiz_review

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
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

        interface WithInstruction {
            val instruction: ReviewInstruction
        }

        interface WithProgress {
            val progress: Progress?
        }

        interface WithQuizState {
            val quizState: StepQuizView.State
        }

        data class SubmissionNotMade(
            override val quizState: StepQuizView.State,
            override val progress: Progress?
        ) : State(), WithProgress, WithQuizState // 1

        data class SubmissionNotSelected(
            override val quizState: StepQuizView.State.AttemptLoaded,
            val isSessionCreationInProgress: Boolean,
            override val progress: Progress?
        ) : State(), WithProgress, WithQuizState // 2

        data class SubmissionSelected(
            override val quizState: StepQuizView.State.AttemptLoaded,
            val isReviewCreationInProgress: Boolean,
            val session: ReviewSession,
            override val instruction: ReviewInstruction,
            override val progress: Progress?
        ) : State(), WithInstruction, WithProgress, WithQuizState // 3

        data class Completed(
            override val quizState: StepQuizView.State.AttemptLoaded,
            val session: ReviewSession,
            override val instruction: ReviewInstruction,
            override val progress: Progress?
        ) : State(), WithInstruction, WithProgress, WithQuizState // 3 / 5
    }

    sealed class Message {
        /**
         * Initialization
         */
        data class InitWithStep(val stepWrapper: StepPersistentWrapper, val lessonData: LessonData, val forceUpdate: Boolean = false) : Message()
        data class FetchReviewSessionSuccess(
            val sessionData: ReviewSessionData,
            val instruction: ReviewInstruction,
            val progress: Progress? // assignment progress
        ) : Message()
        data class FetchStepQuizStateSuccess(
            val quizState: StepQuizView.State,
            val progress: Progress? // assignment progress
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
        data class SessionCreated(val reviewSession: ReviewSession, val instruction: ReviewInstruction) : Message() // solution selected and session created

        /**
         * Starting review
         */
        object StartReviewWithCurrentSession : Message()
        object StartReviewError : Message()
        data class ReviewCreated(val reviewId: Long) : Message()
    }

    sealed class Action {
        data class FetchStepQuizState(val stepWrapper: StepPersistentWrapper, val lessonData: LessonData) : Action() // if there is no review session
        data class FetchReviewSession(
            val stepId: Long,
            val unitId: Long?,
            val instructionId: Long,
            val sessionId: Long
        ) : Action()
        data class CreateSessionWithSubmission(val submissionId: Long) : Action() // select solution

        data class CreateReviewWithSession(val sessionId: Long) : Action()

        sealed class ViewAction : Action() {
            object ShowNetworkError : ViewAction() // error
            data class OpenReviewScreen(val reviewId: Long) : ViewAction()
        }
    }

    fun render(state: State)
    fun onAction(action: Action.ViewAction)
}