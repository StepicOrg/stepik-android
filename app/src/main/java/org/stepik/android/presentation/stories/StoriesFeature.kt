package org.stepik.android.presentation.stories

import ru.nobird.android.stories.model.Story

interface StoriesFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        data class Success(val stories: List<Story>, val viewedStoriesIds: Set<Long>) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchStoriesSuccess(val stories: List<Story>, val viewedStoriesIds: Set<Long>) : Message()
        data class StoryViewed(val storyId: Long) : Message()
        object FetchStoriesError : Message()
    }

    sealed class Action {
        object FetchStories : Action()
        data class MarkStoryAsViewed(val storyId: Long) : Action()
        sealed class ViewAction : Action()
    }
}