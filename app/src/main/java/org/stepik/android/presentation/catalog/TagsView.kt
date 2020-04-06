package org.stepik.android.presentation.catalog

import org.stepik.android.model.Tag

interface TagsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        data class TagsLoaded(val tags: List<Tag>) : State()
    }

    fun setState(state: State)
}