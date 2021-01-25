package org.stepik.android.presentation.story_deeplink

import org.stepik.android.model.StoryTemplate

interface StoryDeepLinkView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        data class Success(val story: StoryTemplate) : State()
    }

    fun setState(state: State)
}