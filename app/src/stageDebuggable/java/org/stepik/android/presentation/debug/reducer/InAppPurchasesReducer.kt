package org.stepik.android.presentation.debug.reducer

import org.stepik.android.presentation.debug.InAppPurchasesFeature.State
import org.stepik.android.presentation.debug.InAppPurchasesFeature.Message
import org.stepik.android.presentation.debug.InAppPurchasesFeature.Action
import ru.nobird.app.core.model.mutate

import ru.nobird.app.presentation.redux.reducer.StateReducer
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
            is Message.PurchaseClickedMessage -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowLoading, Action.ConsumePurchase(message.purchase))
                } else {
                    null
                }
            }
            is Message.ConsumeAllMessage -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowLoading, Action.ConsumeAllPurchases(state.purchases))
                } else {
                    null
                }
            }
            is Message.ConsumeSuccess -> {
                if (state is State.Content) {
                    val updatedPurchases = state.purchases.mapNotNull {
                        if (it.skus.first() == message.purchase.skus.first()) {
                            null
                        } else {
                            it
                        }
                    }
                    State.Content(updatedPurchases)to setOf(Action.ViewAction.ShowConsumeSuccess)
                } else {
                    null
                }
            }
            is Message.ConsumeAllSuccess -> {
                if (state is State.Content) {
                    State.Empty to setOf(Action.ViewAction.ShowConsumeSuccess)
                } else {
                    null
                }
            }
            is Message.ConsumeFailure -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowConsumeFailure)
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}
