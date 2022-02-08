package org.stepik.android.presentation.course_news

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_news.model.CourseNewsListItem

interface CourseNewsFeature {
    sealed class State {
        data class Idle(val mustFetchRemote: Boolean = false) : State()
        data class Empty(val announcementIds: List<Long>, val isTeacher: Boolean) : State()
        object NotEnrolled : State()
        object Error : State()
        data class LoadingAnnouncements(val announcementIds: List<Long>, val isTeacher: Boolean, val sourceType: DataSourceType) : State()
        data class Content(
            val announcementIds: List<Long>,
            val courseNewsListItems: List<CourseNewsListItem.Data>,
            val isTeacher: Boolean,
            val sourceType: DataSourceType, // Necessary for next page loading
            val isLoadingRemote: Boolean, // Needed to block next page loading, when cache is loaded and we are loading remote
            val isLoadingNextPage: Boolean
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val announcementIds: List<Long>, val isTeacher: Boolean) : Message()
        object OnScreenOpenedMessage : Message()

        data class FetchAnnouncementIdsFailure(val throwable: Throwable) : Message()

        data class FetchCourseNewsSuccess(val courseNewsListItems: List<CourseNewsListItem.Data>) : Message()
        object FetchCourseNewsFailure : Message()

        object FetchNextPage : Message()
        data class FetchCourseNewsNextPageSuccess(val courseNewsListItems: List<CourseNewsListItem.Data>) : Message()
        object FetchCourseNewsNextPageFailure : Message()
    }

    sealed class Action {
        data class FetchAnnouncements(val announcementIds: List<Long>, val sourceType: DataSourceType, val isNextPage: Boolean = false) : Action()
        sealed class ViewAction : Action()
    }
}