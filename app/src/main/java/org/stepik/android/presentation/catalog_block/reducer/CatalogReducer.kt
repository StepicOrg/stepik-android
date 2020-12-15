package org.stepik.android.presentation.catalog_block.reducer

import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.presentation.catalog_block.CatalogFeature
import org.stepik.android.presentation.catalog_block.CatalogFeature.State
import org.stepik.android.presentation.catalog_block.CatalogFeature.Message
import org.stepik.android.presentation.catalog_block.CatalogFeature.Action
import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature
import org.stepik.android.presentation.course_continue_redux.reducer.CourseContinueReducer
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.mapper.CourseListStateMapper
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.presentation.course_list_redux.reducer.CourseListReducer
import org.stepik.android.presentation.enrollment.EnrollmentFeature
import org.stepik.android.presentation.filter.reducer.FiltersReducer
import org.stepik.android.presentation.progress.ProgressFeature
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import org.stepik.android.presentation.user_courses.UserCoursesFeature
import ru.nobird.android.core.model.mutate
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CatalogReducer
@Inject
constructor(
    private val storiesReducer: StoriesReducer,
    private val filtersReducer: FiltersReducer,
    private val courseListReducer: CourseListReducer,
    private val courseContinueReducer: CourseContinueReducer,
    private val courseListStateMapper: CourseListStateMapper
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Idle ||
                        state.collectionsState is CatalogFeature.CollectionsState.Error && message.forceUpdate
                ) {
                    state.copy(collectionsState = CatalogFeature.CollectionsState.Loading) to setOf(Action.FetchCatalogBlocks)
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksSuccess -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Loading) {
                    val collections = message.collections.mapNotNull { catalogBlockItem ->
                        when (catalogBlockItem.content) {
                            is CatalogBlockContent.FullCourseList ->
                                CatalogBlockStateWrapper.CourseList(catalogBlockItem = catalogBlockItem, state = CourseListFeature.State.Idle)
                            else ->
                                null
                        }
                    }
                    state.copy(collectionsState = CatalogFeature.CollectionsState.Content(collections)) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksError -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Loading) {
                    state.copy(collectionsState = CatalogFeature.CollectionsState.Error) to emptySet()
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

            is Message.CourseContinueMessage -> {
                val (courseContinueState, courseContinueActions) = courseContinueReducer.reduce(state.courseContinueState, message.message)
                state.copy(courseContinueState = courseContinueState) to courseContinueActions.map {
                    if (it is CourseContinueFeature.Action.ViewAction) {
                        Action.ViewAction.CourseContinueViewAction(it)
                    } else {
                        Action.CourseContinueAction(it)
                    }
                }.toSet()
            }

            is Message.UserCourseMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Content && message.message is UserCoursesFeature.Message.UserCourseOperationUpdate) {
                    val updatedCollection = updateCourseLists(state.collectionsState.collections) { item ->
                        val updatedState = courseListStateMapper.mapToUserCourseUpdate(item.state, message.message.userCourse)
                        CatalogBlockStateWrapper.CourseList(item.catalogBlockItem, updatedState)
                    }
                    state.copy(collectionsState = state.collectionsState.copy(collections = updatedCollection)) to emptySet()
                } else {
                    null
                }
            }

            is Message.ProgressMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Content && message.message is ProgressFeature.Message.ProgressUpdate) {
                    val updatedCollection = updateCourseLists(state.collectionsState.collections) { item ->
                        val updatedState = courseListStateMapper.mergeWithCourseProgress(item.state, message.message.progress)
                        CatalogBlockStateWrapper.CourseList(item.catalogBlockItem, updatedState)
                    }
                    state.copy(collectionsState = state.collectionsState.copy(collections = updatedCollection)) to emptySet()
                } else {
                    null
                }
            }

            is Message.EnrollmentMessage -> {
                if (state.collectionsState is CatalogFeature.CollectionsState.Content && message.message is EnrollmentFeature.Message.EnrollmentMessage) {
                    val courseListActions = mutableSetOf<CourseListFeature.Action>()
                    val updatedCollection = updateCourseLists(state.collectionsState.collections) { item ->
                        item.catalogBlockItem.content.safeCast<CatalogBlockContent.FullCourseList>()?.let {
                            courseListActions.add(CourseListFeature.Action.FetchCourseListAfterEnrollment(item.id, message.message.enrolledCourse, it))
                        }
                        val updatedState = courseListStateMapper.mapToEnrollmentUpdateState(item.state, message.message.enrolledCourse)
                        CatalogBlockStateWrapper.CourseList(item.catalogBlockItem, updatedState)
                    }
                    state.copy(collectionsState = state.collectionsState.copy(collections = updatedCollection)) to courseListActions.map(Action::CourseListAction).toSet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()

    private fun updateCourseLists(
        collections: List<CatalogBlockStateWrapper>,
        mapper: (CatalogBlockStateWrapper.CourseList) -> CatalogBlockStateWrapper
    ): List<CatalogBlockStateWrapper> =
        collections.map { item ->
            if (item is CatalogBlockStateWrapper.CourseList) {
                mapper(item)
            } else {
                item
            }
        }
}