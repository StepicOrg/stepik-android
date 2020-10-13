package org.stepik.android.presentation.step_quiz.reducer

import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.StepQuizView.State
import org.stepik.android.presentation.step_quiz.StepQuizView.Message
import org.stepik.android.presentation.step_quiz.StepQuizView.Action
import org.stepik.android.presentation.base.reducer.StateReducer
import java.util.Calendar
import javax.inject.Inject

class StepQuizReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitWithStep ->
                if (state is State.Idle ||
                    state is State.NetworkError && message.forceUpdate
                ) {
                    State.Loading to setOf(Action.FetchAttempt(message.stepWrapper, message.lessonData))
                } else {
                    null
                }

            is Message.FetchAttemptSuccess ->
                if (state is State.Loading) {
                    State.AttemptLoaded(message.attempt, message.submissionState, message.restrictions) to emptySet()
                } else {
                    null
                }

            is Message.FetchAttemptError ->
                if (state is State.Loading) {
                    State.NetworkError to emptySet()
                } else {
                    null
                }

            is Message.CreateAttemptClicked ->
                if (state is State.AttemptLoaded) {
                    State.AttemptLoading(state.restrictions) to setOf(Action.CreateAttempt(message.step, state.attempt, state.submissionState))
                } else {
                    null
                }

            is Message.CreateAttemptSuccess ->
                if (state is State.AttemptLoading) {
                    State.AttemptLoaded(message.attempt, message.submissionState, state.restrictions) to emptySet()
                } else {
                    null
                }

            is Message.CreateAttemptError ->
                if (state is State.AttemptLoading) {
                    State.NetworkError to emptySet()
                } else {
                    null
                }

            is Message.CreateSubmissionClicked ->
                if (state is State.AttemptLoaded) {
                    val submission = Submission(attempt = state.attempt.id, _reply = message.reply, status = Submission.Status.EVALUATION)

                    state.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission)) to
                            setOf(
                                Action.SaveLocalSubmission(createLocalSubmission(state, message.reply)),
                                Action.CreateSubmission(message.step, state.attempt.id, message.reply)
                            )
                } else {
                    null
                }

            is Message.CreateSubmissionSuccess ->
                if (state is State.AttemptLoaded) {
                    state.copy(
                        submissionState = StepQuizView.SubmissionState.Loaded(message.submission),
                        restrictions = state.restrictions.copy(submissionCount = state.restrictions.submissionCount + 1)
                    ) to emptySet()
                } else {
                    null
                }

            is Message.CreateSubmissionError ->
                if (state is State.AttemptLoaded && state.submissionState is StepQuizView.SubmissionState.Loaded) {
                    val submission = state.submissionState.submission.copy(status = Submission.Status.LOCAL)

                    state.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission)) to setOf(Action.ViewAction.ShowNetworkError)
                } else {
                    null
                }

            is Message.SyncReply ->
                if (state is State.AttemptLoaded) {
                    val submission = createLocalSubmission(state, message.reply)

                    state.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission)) to setOf(Action.SaveLocalSubmission(submission))
                } else {
                    null
                }
        } ?: state to emptySet()

    private fun createLocalSubmission(oldState: State.AttemptLoaded, reply: Reply): Submission {
        val submissionId = (oldState.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission
            ?.id
            ?: 0

        return Submission(id = submissionId, attempt = oldState.attempt.id, _reply = reply, status = Submission.Status.LOCAL, time = Calendar.getInstance().time)
    }
}