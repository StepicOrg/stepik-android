package org.stepik.android.presentation.debug.reducer

import org.stepik.android.presentation.debug.DebugFeature.State
import org.stepik.android.presentation.debug.DebugFeature.Message
import org.stepik.android.presentation.debug.DebugFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class DebugReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || state is State.Error && message.forceUpdate) {
                    State.Loading to setOf(Action.FetchDebugSettings)
                } else {
                    null
                }
            }

            is Message.FetchDebugSettingsSuccess -> {
                if (state is State.Loading) {
                    State.Content(message.debugSettings.fcmToken) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchDebugSettingsFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}