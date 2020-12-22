package org.stepik.android.presentation.story

import org.stepik.android.domain.story.model.StoryReaction

interface StoryFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Content(val reactions: Map<Long, StoryReaction>) : State()
    }

    sealed class Message {
        object Init : Message()

        data class VoteFetchSuccess(
            val reactions: Map<Long, StoryReaction>
        ) : Message()

        data class OnReactionClicked(
            val storyId: Long,
            val storyPosition: Int,
            val reaction: StoryReaction
        ) : Message()
    }

    sealed class Action {
        object FetchVotes : Action()

        data class SaveReaction(
            val storyId: Long,
            val storyPosition: Int,
            val reaction: StoryReaction
        ) : Action()

        sealed class ViewAction : Action()
    }
}