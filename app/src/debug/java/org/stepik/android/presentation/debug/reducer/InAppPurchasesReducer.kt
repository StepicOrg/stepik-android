package org.stepik.android.presentation.debug.reducer

import org.stepik.android.presentation.debug.InAppPurchasesFeature.State
import org.stepik.android.presentation.debug.InAppPurchasesFeature.Message
import org.stepik.android.presentation.debug.InAppPurchasesFeature.Action

import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class InAppPurchasesReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchPurchases)
                } else {
                    null
                }
            }
            is Message.FetchPurchasesSuccess -> {
                if (state is State.Loading) {
                    val newState = if (message.purchases.isEmpty()) {
                        State.Empty
                    } else {
                        State.Content(message.purchases)
                    }
                    newState to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchPurchasesFailure -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}
