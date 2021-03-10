package org.stepik.android.presentation.step_quiz.reducer

import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.StepQuizFeature.State
import org.stepik.android.presentation.step_quiz.StepQuizFeature.Message
import org.stepik.android.presentation.step_quiz.StepQuizFeature.Action
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import ru.nobird.android.presentation.redux.reducer.StateReducer
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

                    state.copy(submissionState = StepQuizFeature.SubmissionState.Loaded(submission)) to
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
                        submissionState = StepQuizFeature.SubmissionState.Loaded(message.submission),
                        restrictions = state.restrictions.copy(submissionCount = state.restrictions.submissionCount + 1)
                    ) to emptySet()
                } else {
                    null
                }

            is Message.CreateSubmissionError ->
                if (state is State.AttemptLoaded && state.submissionState is StepQuizFeature.SubmissionState.Loaded) {
                    val submission = state.submissionState.submission.copy(status = Submission.Status.LOCAL)

                    state.copy(submissionState = StepQuizFeature.SubmissionState.Loaded(submission)) to setOf(Action.ViewAction.ShowNetworkError)
                } else {
                    null
                }

            is Message.SyncReply ->
                if (state is State.AttemptLoaded && !StepQuizFormResolver.isSubmissionInTerminalState(state)) {
                    val submission = createLocalSubmission(state, message.reply)

                    state.copy(submissionState = StepQuizFeature.SubmissionState.Loaded(submission)) to setOf(Action.SaveLocalSubmission(submission))
                } else {
                    null
                }

            is Message.CreateCodePreference ->
                if (state is State.AttemptLoaded) {
                    state to setOf(Action.SaveCodePreference(CodePreference(message.languagesKey, message.language)), Action.PublishCodePreference(message.initCodePreference))
                } else {
                    null
                }

            is Message.InitWithCodePreference -> {
                if (state is State.AttemptLoaded) {
                    val newState: State.AttemptLoaded = when (state.submissionState) {
                        is StepQuizFeature.SubmissionState.Empty -> {
                            state.copy(submissionState = StepQuizFeature.SubmissionState.Empty(Reply(language = message.initCodePreference.language, code = message.initCodePreference.codeTemplates[message.initCodePreference.language])))
                        }
                        is StepQuizFeature.SubmissionState.Loaded -> {
                            val codeFromSubmission = state.submissionState.submission.reply?.code
                            val codeTemplate = message.initCodePreference.codeTemplates[state.submissionState.submission.reply?.language]
                            if ((message.initCodePreference.sourceStepId == state.attempt.step || codeFromSubmission == codeTemplate) && state.submissionState.submission.status == Submission.Status.LOCAL) {
                                state.copy(
                                    submissionState = state.submissionState.copy(
                                        submission = state.submissionState.submission.copy(
                                            _reply = state.submissionState.submission._reply?.copy(
                                                language = message.initCodePreference.language,
                                                code = message.initCodePreference.codeTemplates[message.initCodePreference.language]
                                            )
                                        )
                                    )
                                )
                            } else {
                                state
                            }
                        }
                        else ->
                            throw IllegalArgumentException()
                    }
                    newState to emptySet<Action>()
                } else {
                    null
                }
            }
        } ?: state to emptySet()

    private fun createLocalSubmission(oldState: State.AttemptLoaded, reply: Reply): Submission {
        val submissionId = (oldState.submissionState as? StepQuizFeature.SubmissionState.Loaded)
            ?.submission
            ?.id
            ?: 0

        return Submission(id = submissionId, attempt = oldState.attempt.id, _reply = reply, status = Submission.Status.LOCAL, time = Calendar.getInstance().time)
    }
}