package org.stepik.android.presentation.step_quiz_review.reducer

import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.reducer.StepQuizReducer
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.Action
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.Message
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.State
import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class StepQuizReviewTeacherReducer
@Inject
constructor(
    private val stepQuizReducer: StepQuizReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(
        state: State,
        message: Message
    ): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitWithStep ->
                if (state is State.Idle ||
                    state is State.Error && message.forceUpdate) {
                    State.Loading to setOf(Action.FetchData(message.stepWrapper, message.lessonData, message.instructionType))
                } else {
                    null
                }

            is Message.FetchDataSuccess ->
                if (state is State.Loading) {
                    val quizInitMessage =
                        StepQuizFeature.Message.InitWithStep(message.stepWrapper, message.lessonData)

                    val (quizState, actions) = stepQuizReducer.reduce(StepQuizFeature.State.Idle, quizInitMessage)

                    val newState =
                        State.Data(message.instructionType, message.availableReviewCount, quizState)

                    newState to actions.map(Action::StepQuizAction).toSet()
                } else {
                    null
                }

            is Message.FetchDataError ->
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }

            is Message.StepQuizMessage ->
                if (state is State.Data) {
                    val (quizState, actions) = stepQuizReducer.reduce(state.quizState, message.message)

                    val viewActions = actions
                        .asSequence()
                        .filterIsInstance<StepQuizFeature.Action.ViewAction>()
                        .map { action ->
                            when (action) {
                                is StepQuizFeature.Action.ViewAction.ShowNetworkError ->
                                    Action.ViewAction.ShowNetworkError
                            }
                        }
                        .toSet()

                    state.copy(quizState = quizState) to (actions.map(Action::StepQuizAction).toSet() + viewActions)
                } else {
                    null
                }
        } ?: state to emptySet()
}
