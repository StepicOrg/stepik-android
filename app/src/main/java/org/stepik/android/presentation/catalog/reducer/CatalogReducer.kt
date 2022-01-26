package org.stepik.android.presentation.catalog.reducer

import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.catalog.model.CatalogBlockContent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.presentation.catalog.CatalogFeature
import org.stepik.android.presentation.catalog.CatalogFeature.State
import org.stepik.android.presentation.catalog.CatalogFeature.Message
import org.stepik.android.presentation.catalog.CatalogFeature.Action
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
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import org.stepik.android.presentation.user_courses.UserCoursesFeature
import org.stepik.android.presentation.wishlist.WishlistFeature
import ru.nobird.app.core.model.safeCast
import ru.nobird.app.presentation.redux.reducer.StateReducer
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
                    val collections = message.collections
                        .sortedBy(CatalogBlock::position)
                        .mapNotNull { catalogBlockItem ->
                            when (catalogBlockItem.content) {
                                is CatalogBlockContent.FullCourseList ->
                                    CatalogBlockStateWrapper.FullCourseList(catalogBlock = catalogBlockItem, state = CourseListFeature.State.Idle)

                                is CatalogBlockContent.SimpleCourseLists -> {
                                    if (catalogBlockItem.appearance == CatalogBlockContent.APPEARANCE_SIMPLE_COURSE_LISTS_GRID) {
                                        CatalogBlockStateWrapper.SimpleCourseListsGrid(catalogBlockItem, catalogBlockItem.content)
                                    } else {
                                        CatalogBlockStateWrapper.SimpleCourseListsDefault(catalogBlockItem, catalogBlockItem.content)
                                    }
                                }

                                is CatalogBlockContent.AuthorsList ->
                                    CatalogBlockStateWrapper.AuthorList(catalogBlockItem, catalogBlockItem.content)

                                is CatalogBlockContent.RecommendedCourses ->
                                    CatalogBlockStateWrapper.RecommendedCourseList(catalogBlockItem, CourseListFeature.State.Idle)

                                is CatalogBlockContent.SpecializationsList ->
                                    CatalogBlockStateWrapper.SpecializationList(catalogBlockItem, catalogBlockItem.content)

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
                val (storiesState, collectionsState, refreshAction) =
                    if (message.message is FiltersFeature.Message.LoadFiltersSuccess) {
                        Triple(
                            StoriesFeature.State.Loading,
                            CatalogFeature.BlocksState.Loading,
                            setOf(Action.StoriesAction(StoriesFeature.Action.FetchStories), Action.FetchCatalogBlocks)
                        )
                    } else {
                        Triple(
                            state.storiesState,
                            state.blocksState,
                            emptySet()
                        )
                    }
                val (filtersState, filtersActions) = filtersReducer.reduce(state.filtersState, message.message)
                state.copy(storiesState = storiesState, blocksState = collectionsState, filtersState = filtersState) to filtersActions.map(Action::FiltersAction).toSet() + refreshAction
            }

            is Message.CourseListMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content) {
                    val courseListActionsSet = mutableSetOf<CourseListFeature.Action>()

                    val blocks = state.blocksState
                        .blocks
                        .map { collection ->
                            if (collection.id == message.id) {
                                when (collection) {
                                    is CatalogBlockStateWrapper.FullCourseList -> {
                                        val (courseListState, courseListActions) =
                                            courseListReducer.reduce(collection.state, message.message)

                                        courseListActionsSet += courseListActions
                                        collection.copy(state = courseListState)
                                    }
                                    is CatalogBlockStateWrapper.RecommendedCourseList -> {
                                        val (courseListState, courseListActions) =
                                            courseListReducer.reduce(collection.state, message.message)

                                        courseListActionsSet += courseListActions
                                        collection.copy(state = courseListState)
                                    }
                                    else ->
                                        collection
                                }
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
                        when (item) {
                            is CatalogBlockStateWrapper.FullCourseList ->
                                item.copy(state = mapUserCourseMessageToCourseListState(item.state, message.message))
                            is CatalogBlockStateWrapper.RecommendedCourseList ->
                                item.copy(state = mapUserCourseMessageToCourseListState(item.state, message.message))
                            else ->
                                return@updateCourseLists null
                        }
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
                        when (item) {
                            is CatalogBlockStateWrapper.FullCourseList ->
                                item.copy(state = mapProgressMessageToCourseListState(item.state, message.message))
                            is CatalogBlockStateWrapper.RecommendedCourseList ->
                                item.copy(state = mapProgressMessageToCourseListState(item.state, message.message))
                            else ->
                                return@updateCourseLists null
                        }
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
                        when (item) {
                            is CatalogBlockStateWrapper.FullCourseList -> {
                                val courseListId = item.catalogBlock.content.safeCast<CatalogBlockContent.FullCourseList>()?.courseList?.id ?: -1
                                courseListActions +=
                                    CourseListFeature.Action.FetchCourseAfterEnrollment(item.id, message.message.enrolledCourse.id, CourseViewSource.Collection(courseListId))
                                item.copy(state = mapEnrollmentMessageToCourseListState(item.state, message.message))
                            }
                            is CatalogBlockStateWrapper.RecommendedCourseList -> {
                                courseListActions +=
                                    CourseListFeature.Action.FetchCourseAfterEnrollment(item.id, message.message.enrolledCourse.id, CourseViewSource.Recommendation)
                                item.copy(state = mapEnrollmentMessageToCourseListState(item.state, message.message))
                            }
                            else ->
                                return@updateCourseLists null
                        }
                    }
                    state.copy(blocksState = state.blocksState.copy(blocks = updatedCollection)) to courseListActions.map(Action::CourseListAction).toSet()
                } else {
                    null
                }
            }

            is Message.WishlistMessage -> {
                if (state.blocksState is CatalogFeature.BlocksState.Content &&
                        message.message is WishlistFeature.Message.WishlistOperationUpdate
                ) {
                    val updatedCollection = updateCourseLists(state.blocksState.blocks) { item ->
                        when (item) {
                            is CatalogBlockStateWrapper.FullCourseList ->
                                item.copy(state = mapWishlistMessageToCourseListState(item.state, message.message))
                            is CatalogBlockStateWrapper.RecommendedCourseList ->
                                item.copy(state = mapWishlistMessageToCourseListState(item.state, message.message))
                            else ->
                                return@updateCourseLists null
                        }
                    }
                    state.copy(blocksState = state.blocksState.copy(blocks = updatedCollection)) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()

    private fun updateCourseLists(
        blocks: List<CatalogBlockStateWrapper>,
        mapper: (CatalogBlockStateWrapper) -> CatalogBlockStateWrapper?
    ): List<CatalogBlockStateWrapper> =
        blocks.mapNotNull { item ->
            if (item is CatalogBlockStateWrapper.FullCourseList || item is CatalogBlockStateWrapper.RecommendedCourseList) {
                mapper(item)
            } else {
                item
            }
        }

    private fun mapUserCourseMessageToCourseListState(courseListState: CourseListFeature.State, message: UserCoursesFeature.Message.UserCourseOperationUpdate): CourseListFeature.State =
        courseListStateMapper.mapToUserCourseUpdate(courseListState, message.userCourse)

    private fun mapProgressMessageToCourseListState(courseListState: CourseListFeature.State, message: ProgressFeature.Message.ProgressUpdate): CourseListFeature.State =
        courseListStateMapper.mergeWithCourseProgress(courseListState, message.progress)

    private fun mapEnrollmentMessageToCourseListState(courseListState: CourseListFeature.State, message: EnrollmentFeature.Message.EnrollmentMessage): CourseListFeature.State =
        courseListStateMapper.mapToEnrollmentUpdateState(courseListState, message.enrolledCourse)

    private fun mapWishlistMessageToCourseListState(courseListState: CourseListFeature.State, message: WishlistFeature.Message.WishlistOperationUpdate): CourseListFeature.State =
        courseListStateMapper.mapToWishlistUpdate(courseListState, message.wishlistOperationData)
}