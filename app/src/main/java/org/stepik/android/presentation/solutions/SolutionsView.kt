package org.stepik.android.presentation.solutions

import org.stepik.android.domain.solutions.model.SolutionItem

interface SolutionsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class SolutionsLoaded(val solutions: List<SolutionItem>, val isSending: Boolean) : State()
    }

    fun setState(state: State)
    fun setBlockingLoading(isLoading: Boolean)
    fun onFinishedSending()
    fun showNetworkError()
}