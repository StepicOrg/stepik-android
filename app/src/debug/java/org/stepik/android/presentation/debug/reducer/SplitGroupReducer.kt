package org.stepik.android.presentation.debug.reducer

import org.stepik.android.presentation.debug.SplitGroupFeature.State
import org.stepik.android.presentation.debug.SplitGroupFeature.Message
import org.stepik.android.presentation.debug.SplitGroupFeature.Action
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class SplitGroupReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchSplitGroupData(message.splitGroups))
                } else {
                    null
                }
            }
            is Message.InitSuccess -> {
                if (state is State.Loading) {
                    State.Content(message.splitGroupDataList) to emptySet()
                } else {
                    null
                }
            }
            is Message.ChosenGroup -> {
                if (state is State.Content) {
                    state to setOf(Action.SetSplitGroupData(message.splitGroupData))
                } else {
                    null
                }
            }
            is Message.SetSplitGroupDataSuccess -> {
                if (state is State.Content) {
                    val updatedList = state.splitGroupDataList.mutate {
                        val index = state.splitGroupDataList.indexOfFirst { it.splitTestName == message.splitGroupData.splitTestName }
                        if (index == -1) {
                            add(message.splitGroupData)
                        } else {
                            set(index, message.splitGroupData)
                        }
                    }
                    state.copy(splitGroupDataList = updatedList) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}