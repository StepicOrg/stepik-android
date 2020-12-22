package org.stepik.android.presentation.story

import org.stepik.android.domain.story.model.StoryVote

interface StoryFeature {
    sealed class State {
        object Idle : State()
        data class Content(val votes: Map<Long, StoryVote>) : State()
    }

    sealed class Message {
        data class VoteFetchSuccess(val votes: Map<Long, StoryVote>) : Message()
        data class OnReactionClicked(val storyId: Long, val vote: StoryVote) : Message()
    }

    sealed class Action {
        data class SaveReaction(val storyId: Long, val vote: StoryVote) : Action()
        sealed class ViewAction : Action()
    }
}