package org.stepik.android.presentation.course_continue_redux.reducer

import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature.State
import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature.Message
import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CourseContinueReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitContinueCourse ->
                if (state is State.Idle) {
                    State.Loading to setOf(Action.ContinueCourse(message.course, message.viewSource, message.interactionSource))
                } else {
                    null
                }

            is Message.ShowCourseContinue ->
                if (state is State.Loading) {
                    State.Idle to setOf(Action.ViewAction.ShowCourse(message.course, message.viewSource, message.isAdaptive))
                } else {
                    null
                }

            is Message.ShowStepsContinue ->
                if (state is State.Loading) {
                    State.Idle to setOf(Action.ViewAction.ShowSteps(message.course, message.viewSource, message.lastStep))
                } else {
                    null
                }
        } ?: state to emptySet()
}