package org.stepik.android.presentation.attempts

import org.stepik.android.model.Submission
import org.stepik.android.view.attempts.model.AttemptCacheItem

interface AttemptsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        class AttemptsLoaded(val attempts: List<AttemptCacheItem>) : State()
        class AttemptsSending(val submission: Submission? = null) : State()
        object AttemptsSent : State()
    }

    fun setState(state: State)
    fun setBlockingLoading(isLoading: Boolean)
}