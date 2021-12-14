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
                if (state is State.Idle || (state is State.Error && message.forceUpdate)) {
                    State.Loading to setOf(Action.FetchLessonDemoData(message.course))
                } else {
                    null
                }
            }

            is Message.FetchLessonDemoDataSuccess -> {
                if (state is State.Loading) {
                    State.Content(message.deeplinkPromoCode, message.coursePurchaseData) to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchLessonDemoDataFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
            is Message.BuyActionMessage -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.BuyAction(state.deeplinkPromoCode, state.coursePurchaseData))
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}