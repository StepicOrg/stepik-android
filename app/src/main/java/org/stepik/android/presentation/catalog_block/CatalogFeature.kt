package org.stepik.android.presentation.catalog_block

import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.stories.StoriesFeature

interface CatalogFeature {
    data class State(
        val storiesState: StoriesFeature.State
        // filtersState
//        val collectionsState: CollectionsState
    )

    sealed class CollectionsState {
        object Idle : CollectionsState()
        object Loading : CollectionsState()
        object Error : CollectionsState()
        class Content(val collections: List<CourseListCollectionPresenter>) : CollectionsState()
    }

    sealed class Message {
//        object InitMessage : Message()
        /**
         * Stories Message Wrapper
         */
        data class StoriesMessage(val message: StoriesFeature.Message) : Message()
    }

    sealed class Action {
        object FetchCatalogBlocks : Action()

        /**
         * Stories Action Wrapper
         */
        data class StoriesAction(val action: StoriesFeature.Action) : Action()

        sealed class ViewAction : Action() {

        }
    }
}