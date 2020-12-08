package org.stepik.android.presentation.catalog_block.reducer

import org.stepik.android.presentation.catalog_block.CatalogFeature
import org.stepik.android.presentation.catalog_block.CatalogFeature.State
import org.stepik.android.presentation.catalog_block.CatalogFeature.Message
import org.stepik.android.presentation.catalog_block.CatalogFeature.Action
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.presentation.course_list_redux.reducer.CourseListReducer
import org.stepik.android.presentation.filter.reducer.FiltersReducer
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CatalogReducer
@Inject
constructor(
    private val storiesReducer: StoriesReducer,
    private val filtersReducer: FiltersReducer,
    private val courseListReducer: CourseListReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Idle ||
                        state.collectionsState is CatalogFeature.CollectionsState.Error && message.forceUpdate
                ) {
                    val (collectionsState, collectionsAction) = CatalogFeature.CollectionsState.Loading to setOf(Action.FetchCatalogBlocks)
                    state.copy(collectionsState = collectionsState) to collectionsAction
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksSuccess -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Loading) {
                    state.copy(collectionsState = CatalogFeature.CollectionsState.Content(message.collections)) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksError -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Loading) {
                    val (collectionState, collectionsAction) = CatalogFeature.CollectionsState.Error to emptySet<Action>()
                    state.copy(collectionsState = collectionState) to collectionsAction
                } else {
                    null
                }
            }

            is Message.StoriesMessage -> {
                val (storiesState, storiesActions) = storiesReducer.reduce(state.storiesState, message.message)
                state.copy(storiesState = storiesState) to storiesActions.map(Action::StoriesAction).toSet()
            }

            is Message.FiltersMessage -> {
                val (filtersState, filtersActions) = filtersReducer.reduce(state.filtersState, message.message)
                state.copy(filtersState = filtersState) to filtersActions.map(Action::FiltersAction).toSet()
            }

            is Message.CourseListMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Content) {
                    val updateIndex = state.collectionsState.collections.indexOfFirst { it.id == message.id }
                    val updateState = state.collectionsState.collections[updateIndex] as CatalogBlockStateWrapper.CourseList
                    val (courseListState, courseListActions) = courseListReducer.reduce(updateState.state, message.message)
                    val result = state.copy(collectionsState = state.collectionsState.copy(state.collectionsState.collections.mutate { set(updateIndex, updateState.copy(state = courseListState)) }))
                    result to courseListActions.map(Action::CourseListAction).toSet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}