package org.stepik.android.presentation.filter.reducer

import org.stepik.android.presentation.filter.FiltersFeature.State
import org.stepik.android.presentation.filter.FiltersFeature.Message
import org.stepik.android.presentation.filter.FiltersFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class FiltersReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage ->
                if (state is State.Idle || message.forceUpdate) {
                    State.Loading to setOf(Action.LoadFilters)
                } else {
                    null
                }

            is Message.LoadFiltersSuccess ->
                if (state is State.Loading) {
                    State.FiltersLoaded(message.filters) to emptySet()
                } else {
                    null
                }

            is Message.LoadFiltersError ->
                if (state is State.Loading) {
                    State.Empty to emptySet()
                } else {
                    null
                }

            is Message.FiltersChanged ->
                if (state is State.FiltersLoaded) {
                    State.FiltersLoaded(message.filters) to emptySet()
                } else {
                    null
                }
        } ?: state to emptySet()
}