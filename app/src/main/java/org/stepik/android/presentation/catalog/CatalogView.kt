package org.stepik.android.presentation.catalog

import org.stepik.android.presentation.course_list.CourseListCollectionPresenter

interface CatalogView {
    data class State(
        val headers: List<CatalogItem>,
        val collectionsState: CollectionsState,
        val footers: List<CatalogItem>
    )

    sealed class CollectionsState {
        object Idle : CollectionsState()
        object Loading : CollectionsState()
        object Error : CollectionsState()
        class Content(val collections: List<CourseListCollectionPresenter>) : CollectionsState()
    }

    fun setState(state: State)
}