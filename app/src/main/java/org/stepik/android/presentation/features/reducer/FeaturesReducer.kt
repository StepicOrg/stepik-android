package org.stepik.android.presentation.features.reducer

import org.stepik.android.presentation.features.FeaturesFeature.State
import org.stepik.android.presentation.features.FeaturesFeature.Message
import org.stepik.android.presentation.features.FeaturesFeature.Action

import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class FeaturesReducer @Inject constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchRubricatorUrl)
                } else {
                    null
                }
            }

            is Message.FetchRubricatorUrlSuccess -> {
                if (state is State.Loading) {
                    val newState = if (message.rubricatorUrl.isEmpty()) {
                        State.Empty
                    } else {
                        State.Success(message.rubricatorUrl)
                    }
                    newState to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchRubricatorUrlError -> {
                if (state is State.Loading) {
                    State.Empty to emptySet()
                } else {
                    null
                }
            }
        } ?: (state to emptySet())
}