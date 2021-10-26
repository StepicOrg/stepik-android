package org.stepik.android.presentation.debug.reducer

import org.stepik.android.presentation.debug.SplitTestsFeature.State
import org.stepik.android.presentation.debug.SplitTestsFeature.Message
import org.stepik.android.presentation.debug.SplitTestsFeature.Action
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class SplitTestsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchSplitTestData(message.splitTests))
                } else {
                    null
                }
            }
            is Message.InitSuccess -> {
                if (state is State.Loading) {
                    State.Content(message.splitTestDataList) to emptySet()
                } else {
                    null
                }
            }
            is Message.ChosenGroup -> {
                if (state is State.Content) {
                    state to setOf(Action.SetSplitTestData(message.splitTestData))
                } else {
                    null
                }
            }
            is Message.SetSplitTestDataSuccess -> {
                if (state is State.Content) {
                    val updatedList = state.splitTestDataList.mutate {
                        val index = state.splitTestDataList.indexOfFirst { it.splitTestName == message.splitTestData.splitTestName }
                        if (index == -1) {
                            add(message.splitTestData)
                        } else {
                            set(index, message.splitTestData)
                        }
                    }
                    state.copy(splitTestDataList = updatedList) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}