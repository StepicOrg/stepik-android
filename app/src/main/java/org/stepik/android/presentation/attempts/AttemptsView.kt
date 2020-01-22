package org.stepik.android.presentation.attempts

import org.stepik.android.view.attempts.model.AttemptCacheItem

interface AttemptsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        class AttemptsLoaded(val attempts: List<AttemptCacheItem>) : State()
    }

    fun setState(state: State)
}