package org.stepik.android.presentation.lesson_demo.reducer

import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.State
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.Message
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class LessonDemoReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchLessonDemoData)
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}