package org.stepik.android.presentation.catalog

interface CatalogView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Content(val collections: List<CatalogItem>) : State()
    }

    fun setState(state: State)
}