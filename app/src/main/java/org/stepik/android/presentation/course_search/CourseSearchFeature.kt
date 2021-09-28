package org.stepik.android.presentation.course_search

import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import ru.nobird.android.core.model.PagedList

interface CourseSearchFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(
            val courseSearchResultListDataItems: PagedList<CourseSearchResultListItem.Data>,
            val courseSearchResultListItems: List<CourseSearchResultListItem>
        ) : State()
    }

    sealed class Message {
        data class FetchCourseSearchResultsInitial(val courseId: Long, val query: String) : Message()
        data class FetchNextPage(val courseId: Long, val query: String) : Message()
        data class FetchCourseSearchResultsSuccess(val courseSearchResultsDataItems: PagedList<CourseSearchResultListItem.Data>) : Message()
        data class FetchCourseSearchResultsNextSuccess(val courseSearchResultsDataItems: PagedList<CourseSearchResultListItem.Data>) : Message()
        object FetchCourseSearchResultsFailure : Message()
        object FetchCourseSearchResultsNextFailure : Message()
    }

    sealed class Action {
        data class FetchCourseSearchResults(val courseId: Long, val query: String, val page: Int = 1) : Action()
        sealed class ViewAction : Action()
    }
}