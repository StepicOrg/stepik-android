package org.stepik.android.presentation.step_quiz_review.reducer

import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView.State
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView.Action
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView.Message
import ru.nobird.android.core.model.safeCast
import javax.inject.Inject

class StepQuizReviewReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(
        state: State,
        message: Message
    ): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitWithStep ->
                if (state is State.Idle ||
                    state is State.Error && message.forceUpdate) {
                    val sessionId = message.step.session
                    val action =
                        if (sessionId != null && sessionId > 0) {
                            Action.FetchReviewSession(message.step.instruction ?: -1, sessionId)
                        } else {
                            Action.FetchStepQuizState(message.step)
                        }

                    State.Loading(message.step) to setOf(action)
                } else {
                    null
                }

            is Message.FetchReviewSessionSuccess ->
                when (state) {
                    is State.Loading -> {
                        val quizState = createAttemptLoadedState(message.attempt, message.submission)
                        val newState =
                            if (message.progress != null) {
                                State.Completed(quizState, message.progress)
                            } else {
                                State.SubmissionSelected(quizState, message.session, message.instruction)
                            }

                        newState to emptySet()
                    }

                    else -> null
                }

            is Message.FetchStepQuizStateSuccess ->
                when (state) {
                    is State.Loading ->
                        State.SubmissionNotMade(
                            quizState = message.quizState,
                            instruction = message.instruction
                        ) to emptySet()
                    else -> null
                }

            is Message.InitialFetchError ->
                when (state) {
                    is State.Loading ->
                        State.Error to emptySet()
                    else -> null
                }

            is Message.SolveAgain ->
                when (state) {
                    is State.SubmissionNotSelected ->
                        State.SubmissionNotMade(state.quizState, state.instruction) to emptySet()

                    else -> null
                }

            is Message.ChangeSubmission ->
                when (state) {
                    is State.SubmissionNotSelected -> {
                        val stepQuizViewState =
                            state.quizState.copy(attempt = message.attempt, submissionState = StepQuizView.SubmissionState.Loaded(message.submission))

                        State.SubmissionNotSelected(stepQuizViewState, state.instruction) to emptySet()
                    }

                    else -> null
                }

            is Message.CreateSessionWithCurrentSubmission ->
                when (state) {
                    is State.SubmissionNotSelected -> {
                        val submissionId = state.quizState.submissionState.safeCast<StepQuizView.SubmissionState.Loaded>()
                            ?.submission
                            ?.id

                        if (submissionId != null) {
                            State.SubmissionSelectedLoading(state.quizState, state.instruction) to setOf(Action.CreateSessionWithSubmission(submissionId))
                        } else {
                            null
                        }
                    }

                    else -> null
                }

            is Message.CreateSessionError ->
                when (state) {
                    is State.SubmissionSelectedLoading ->
                        State.SubmissionNotSelected(state.quizState, state.instruction) to setOf(Action.ViewAction.ShowNetworkError)

                    else -> null
                }

            is Message.SessionCreated ->
                when (state) {
                    is State.SubmissionSelectedLoading ->
                        State.SubmissionSelected(state.quizState, message.reviewSession, state.instruction) to emptySet()

                    else -> null
                }
        } ?: state to emptySet()

    private fun createAttemptLoadedState(attempt: Attempt, submission: Submission, restrictions: StepQuizRestrictions? = null): StepQuizView.State.AttemptLoaded =
        StepQuizView.State.AttemptLoaded(
            attempt = attempt,
            submissionState = StepQuizView.SubmissionState.Loaded(submission),
            restrictions = restrictions ?: StepQuizRestrictions(1, 1, DiscountingPolicyType.NoDiscount)
        )
}