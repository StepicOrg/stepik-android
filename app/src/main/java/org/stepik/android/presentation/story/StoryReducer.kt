package org.stepik.android.presentation.story

import ru.nobird.android.presentation.redux.reducer.StateReducer
import org.stepik.android.presentation.story.StoryFeature.State
import org.stepik.android.presentation.story.StoryFeature.Message
import org.stepik.android.presentation.story.StoryFeature.Action
import javax.inject.Inject

class StoryReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.Init ->
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchVotes)
                } else {
                    null
                }

            is Message.VoteFetchSuccess ->
                if (state is State.Loading) {
                    State.Content(message.votes) to emptySet()
                } else {
                    null
                }

            is Message.OnReactionClicked ->
                if (state is State.Content) {
                    state.copy(votes = state.votes + (message.storyId to message.vote)) to setOf(Action.SaveReaction(message.storyId, message.vote))
                } else {
                    null
                }
        } ?: state to emptySet()
}