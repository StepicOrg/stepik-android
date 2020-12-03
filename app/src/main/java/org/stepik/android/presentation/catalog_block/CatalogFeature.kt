package org.stepik.android.presentation.catalog_block

import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.stories.StoriesFeature

interface CatalogFeature {
    data class State(
        val storiesState: StoriesFeature.State,
        val filtersState: FiltersFeature.State,
        val collectionsState: CollectionsState
    )

    sealed class CollectionsState {
        object Idle : CollectionsState()
        object Loading : CollectionsState()
        object Error : CollectionsState()
        class Content(val collections: List<CatalogBlockStateWrapper>) : CollectionsState()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchCatalogBlocksSuccess(val collections: List<CatalogBlockStateWrapper>) : Message()
        object FetchCatalogBlocksError : Message()
        /**
         * Message Wrappers
         */
        data class StoriesMessage(val message: StoriesFeature.Message) : Message()
        data class FiltersMessage(val message: FiltersFeature.Message) : Message()
    }

    sealed class Action {
        object FetchCatalogBlocks : Action()

        /**
         * Action Wrappers
         */
        data class StoriesAction(val action: StoriesFeature.Action) : Action()
        data class FiltersAction(val action: FiltersFeature.Action) : Action()

        sealed class ViewAction : Action()
    }
}