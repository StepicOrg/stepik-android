package org.stepik.android.presentation.course_list_redux

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.core.model.Identifiable

interface CourseListFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object NetworkError : State()

        data class Content(
            val courseListDataItems: PagedList<CourseListItem.Data>,
            val courseListItems: List<CourseListItem>
        ) : State()
    }

    sealed class Message : Identifiable<String> {
        data class InitMessage(
            override val id: String,
            val courseList: StandardCatalogBlockContentItem,
            val forceUpdate: Boolean = false
        ) : Message()

        data class FetchCourseListSuccess(
            override val id: String,
            val courseListDataItems: PagedList<CourseListItem.Data>,
            val courseListItems: List<CourseListItem>
        ) : Message()

        data class FetchCourseListError(
            override val id: String
        ) : Message()

        data class OnEnrollmentFetchCourseListSuccess(
            override val id: String,
            val courseListItem: CourseListItem.Data
        ) : Message()
    }

    sealed class Action {
        data class FetchCourseList(val id: String, val courseIds: List<Long>, val courseListId: Long) : Action()
        data class FetchCourseAfterEnrollment(val id: String, val courseId: Long, val courseListId: Long) : Action()
        sealed class ViewAction : Action()
    }
}