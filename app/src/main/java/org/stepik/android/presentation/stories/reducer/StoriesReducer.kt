package org.stepik.android.presentation.stories.reducer

import org.stepik.android.presentation.stories.StoriesFeature.State
import org.stepik.android.presentation.stories.StoriesFeature.Message
import org.stepik.android.presentation.stories.StoriesFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class StoriesReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage ->
                if (state is State.Idle || message.forceUpdate) {
                    State.Loading to setOf(Action.FetchStories)
                } else {
                    null
                }

            is Message.FetchStoriesSuccess ->
                if (state is State.Loading) {
                    State.Success(message.stories, message.viewedStoriesIds) to emptySet()
                } else {
                    null
                }

            is Message.FetchStoriesError ->
                if (state is State.Loading) {
                    State.Empty to emptySet()
                } else {
                    null
                }
        } ?: state to emptySet()
}