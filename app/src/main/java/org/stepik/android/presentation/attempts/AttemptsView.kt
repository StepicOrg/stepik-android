package org.stepik.android.presentation.attempts

import org.stepik.android.domain.attempts.model.AttemptCacheItem

interface AttemptsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class AttemptsLoaded(val attempts: List<AttemptCacheItem>, val isSending: Boolean) : State()
    }

    fun setState(state: State)
    fun setBlockingLoading(isLoading: Boolean)
    fun onFinishedSending()
}