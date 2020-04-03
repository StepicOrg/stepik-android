package org.stepik.android.presentation.catalog

interface CatalogView {
    sealed class State {
        class Content(collections: List<CatalogItem>) : State()
    }
}