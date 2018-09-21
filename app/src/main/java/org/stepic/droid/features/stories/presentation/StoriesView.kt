package org.stepic.droid.features.stories.presentation

import ru.nobird.android.stories.model.Story

interface StoriesView {
    fun setState(state: State)

    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        data class Success(val stories: List<Story>, val viewedStoriesIds: Set<Long>) : State()
    }
}