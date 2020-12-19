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
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.filter.reducer.FiltersReducer
import org.stepik.android.presentation.progress.ProgressFeature
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import org.stepik.android.presentation.user_courses.UserCoursesFeature
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
            // <editor-fold desc="Blocks Messages">
            is Message.InitMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Idle ||
                    state.blocksState is CatalogFeature.BlocksState.Error && message.forceUpdate
                ) {
                    state.copy(blocksState = CatalogFeature.BlocksState.Loading) to setOf(Action.FetchCatalogBlocks)
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksSuccess -> {
                if (state.blocksState is CatalogFeature.BlocksState.Loading) {
                    val collections = message.collections.mapNotNull { catalogBlockItem ->
                        when (catalogBlockItem.content) {
                            is CatalogBlockContent.FullCourseList ->
                                CatalogBlockStateWrapper.FullCourseList(catalogBlock = catalogBlockItem, state = CourseListFeature.State.Idle)

                            is CatalogBlockContent.SimpleCourseLists ->
                                CatalogBlockStateWrapper.SimpleCourseListsDefault(catalogBlockItem, catalogBlockItem.content)

                            is CatalogBlockContent.AuthorsList ->
                                CatalogBlockStateWrapper.AuthorList(catalogBlockItem, catalogBlockItem.content)
                            else ->
                                null
                        }
                    }
                    state.copy(blocksState = CatalogFeature.BlocksState.Content(collections)) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchCatalogBlocksError -> {
                if (state.blocksState is CatalogFeature.BlocksState.Loading) {
                    state.copy(blocksState = CatalogFeature.BlocksState.Error) to emptySet()
                } else {
                    null
                }
            }
            // </editor-fold>

            is Message.StoriesMessage -> {
                val (storiesState, storiesActions) = storiesReducer.reduce(state.storiesState, message.message)
                state.copy(storiesState = storiesState) to storiesActions.map(Action::StoriesAction).toSet()
            }

            is Message.FiltersMessage -> {
                val (collectionsState, refreshAction) =
                    if (message.message is FiltersFeature.Message.LoadFiltersSuccess) {
                        CatalogFeature.BlocksState.Loading to setOf(Action.FetchCatalogBlocks)
                    } else {
                        state.blocksState to emptySet()
                    }
                val (filtersState, filtersActions) = filtersReducer.reduce(state.filtersState, message.message)
                state.copy(blocksState = collectionsState, filtersState = filtersState) to filtersActions.map(Action::FiltersAction).toSet() + refreshAction
            }

            is Message.CourseListMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content) {
                    val courseListActionsSet = mutableSetOf<CourseListFeature.Action>()

                    val blocks = state.blocksState
                        .blocks
                        .map { collection ->
                            if (collection.id == message.id &&
                                collection is CatalogBlockStateWrapper.FullCourseList
                            ) {
                                val (courseListState, courseListActions) =
                                    courseListReducer.reduce(collection.state, message.message)

                                courseListActionsSet += courseListActions
                                collection.copy(state = courseListState)
                            } else {
                                collection
                            }
                        }

                    val actions = courseListActionsSet.map(Action::CourseListAction).toSet()

                    state.copy(blocksState = CatalogFeature.BlocksState.Content(blocks)) to actions
                } else {
                    null
                }
            }

            is Message.CourseContinueMessage -> {
                val (courseContinueState, courseContinueActions) = courseContinueReducer.reduce(state.courseContinueState, message.message)

                val actions = courseContinueActions
                    .map {
                        if (it is CourseContinueFeature.Action.ViewAction) {
                            Action.ViewAction.CourseContinueViewAction(it)
                        } else {
                            Action.CourseContinueAction(it)
                        }
                    }
                    .toSet()

                state.copy(courseContinueState = courseContinueState) to actions
            }

            is Message.UserCourseMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content &&
                    message.message is UserCoursesFeature.Message.UserCourseOperationUpdate
                ) {
                    val updatedCollection = updateCourseLists(state.blocksState.blocks) { item ->
                        val updatedState = courseListStateMapper.mapToUserCourseUpdate(item.state, message.message.userCourse)
                        item.copy(state = updatedState)
                    }
                    state.copy(blocksState = state.blocksState.copy(blocks = updatedCollection)) to emptySet()
                } else {
                    null
                }
            }

            is Message.ProgressMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content &&
                    message.message is ProgressFeature.Message.ProgressUpdate
                ) {
                    val updatedCollection = updateCourseLists(state.blocksState.blocks) { item ->
                        val updatedState = courseListStateMapper.mergeWithCourseProgress(item.state, message.message.progress)
                        item.copy(state = updatedState)
                    }
                    state.copy(blocksState = state.blocksState.copy(blocks = updatedCollection)) to emptySet()
                } else {
                    null
                }
            }

            is Message.EnrollmentMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content && message.message is EnrollmentFeature.Message.EnrollmentMessage) {
                    val courseListActions = mutableSetOf<CourseListFeature.Action>()
                    val updatedCollection = updateCourseLists(state.blocksState.blocks) { item ->
                        item.catalogBlock.content.safeCast<CatalogBlockContent.FullCourseList>()?.let {
                            courseListActions +=
                                CourseListFeature.Action.FetchCourseAfterEnrollment(item.id, message.message.enrolledCourse.id, it.courseList.id)
                        }
                        val updatedState = courseListStateMapper.mapToEnrollmentUpdateState(item.state, message.message.enrolledCourse)
                        item.copy(state = updatedState)
                    }
                    state.copy(blocksState = state.blocksState.copy(blocks = updatedCollection)) to courseListActions.map(Action::CourseListAction).toSet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()

    private fun updateCourseLists(
        blocks: List<CatalogBlockStateWrapper>,
        mapper: (CatalogBlockStateWrapper.FullCourseList) -> CatalogBlockStateWrapper
    ): List<CatalogBlockStateWrapper> =
        blocks.map { item ->
            if (item is CatalogBlockStateWrapper.FullCourseList) {
                mapper(item)
            } else {
                item
            }
        }
}