package org.stepik.android.presentation.catalog_block

import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.presentation.course_continue_redux.CourseContinueFeature
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.presentation.enrollment.EnrollmentFeature
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.progress.ProgressFeature
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.user_courses.UserCoursesFeature

interface CatalogFeature {
    data class State(
        val storiesState: StoriesFeature.State,
        val filtersState: FiltersFeature.State,
        val blocksState: BlocksState,
        val courseContinueState: CourseContinueFeature.State
    )

    sealed class BlocksState {
        object Idle : BlocksState()
        object Loading : BlocksState()
        object Error : BlocksState()
        data class Content(val blocks: List<CatalogBlockStateWrapper>) : BlocksState()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchCatalogBlocksSuccess(val collections: List<CatalogBlockItem>) : Message()
        object FetchCatalogBlocksError : Message()

        /**
         * Message Wrappers
         */
        data class StoriesMessage(val message: StoriesFeature.Message) : Message()
        data class FiltersMessage(val message: FiltersFeature.Message) : Message()
        data class CourseListMessage(val id: String, val message: CourseListFeature.Message) : Message()
        data class CourseContinueMessage(val message: CourseContinueFeature.Message) : Message()
        data class UserCourseMessage(val message: UserCoursesFeature.Message) : Message()
        data class ProgressMessage(val message: ProgressFeature.Message) : Message()
        data class EnrollmentMessage(val message: EnrollmentFeature.Message) : Message()
    }

    sealed class Action {
        object FetchCatalogBlocks : Action()

        /**
         * Action Wrappers
         */
        data class StoriesAction(val action: StoriesFeature.Action) : Action()
        data class FiltersAction(val action: FiltersFeature.Action) : Action()
        data class CourseListAction(val action: CourseListFeature.Action) : Action()
        data class CourseContinueAction(val action: CourseContinueFeature.Action) : Action()

        sealed class ViewAction : Action() {
            data class CourseContinueViewAction(val viewAction: CourseContinueFeature.Action.ViewAction) : ViewAction()
        }
    }
}