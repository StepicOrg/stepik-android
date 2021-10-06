package org.stepik.android.presentation.course_search

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import org.stepik.android.model.Step
import org.stepik.android.model.comments.DiscussionThread
import ru.nobird.android.core.model.PagedList

interface CourseSearchFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(
            val courseSearchResultListDataItems: PagedList<CourseSearchResultListItem.Data>,
            val query: String,
            val isLoadingNextPage: Boolean,
            val isSuggestion: Boolean
        ) : State()
    }

    sealed class Message {
        data class FetchCourseSearchResultsInitial(val courseId: Long, val courseTitle: String, val query: String, val isSuggestion: Boolean) : Message()
        data class FetchNextPage(val courseId: Long, val courseTitle: String, val query: String) : Message()
        data class FetchCourseSearchResultsSuccess(val courseSearchResultsDataItems: PagedList<CourseSearchResultListItem.Data>, val query: String, val isSuggestion: Boolean) : Message()
        data class FetchCourseSearchResultsNextSuccess(val courseSearchResultsDataItems: PagedList<CourseSearchResultListItem.Data>) : Message()
        object FetchCourseSearchResultsFailure : Message()
        data class FetchCourseSearchResultsNextFailure(val page: Int) : Message()
        data class CourseContentSearchResultClickedEventMessage(val courseId: Long, val courseTitle: String, val query: String, val type: String, val step: Long?) : Message()
        data class CourseContentSearchedEventMessage(val courseId: Long, val courseTitle: String, val query: String, val isSuggestion: Boolean) : Message()
        data class InitDiscussionThreadMessage(val step: Step, val discussionId: Long?) : Message()
        data class DiscussionThreadSuccess(val step: Step, val discussionThread: DiscussionThread, val discussionId: Long? = null) : Message()
        object DiscussionThreadError : Message()
    }

    sealed class Action {
        data class FetchCourseSearchResults(val courseId: Long, val courseTitle: String, val query: String, val isSuggestion: Boolean, val page: Int = 1) : Action()
        data class FetchDiscussionThread(val step: Step, val discussionId: Long?) : Action()
        data class LogAnalyticEvent(val analyticEvent: AnalyticEvent) : Action()
        sealed class ViewAction : Action() {
            object ShowLoadingDialog : ViewAction()
            object HideLoadingDialog : ViewAction()
            data class OpenComment(val step: Step, val discussionThread: DiscussionThread, val discussionId: Long? = null) : ViewAction()
        }
    }
}