package org.stepik.android.presentation.course_list_redux

import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.catalog.model.CatalogCourseList
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.app.core.model.Identifiable

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
            val courseList: CatalogCourseList,
            val forceUpdate: Boolean = false
        ) : Message()

        data class InitMessageRecommended(
            override val id: String,
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
        data class FetchCourseList(val id: String, val courseIds: List<Long>, val courseViewSource: CourseViewSource) : Action()
        data class FetchCourseAfterEnrollment(val id: String, val courseId: Long, val courseViewSource: CourseViewSource) : Action()
        data class FetchCourseRecommendations(val id: String) : Action()
        sealed class ViewAction : Action()
    }
}