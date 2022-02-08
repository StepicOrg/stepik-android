package org.stepik.android.presentation.course_news

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_news.model.CourseNewsListItem

interface CourseNewsFeature {
    sealed class State {
        data class Idle(val mustFetchRemote: Boolean = false) : State()
        data class Empty(val announcementIds: List<Long>) : State()
        object NotEnrolled : State()
        object Error : State()
        data class LoadingAnnouncements(val announcementIds: List<Long>, val sourceType: DataSourceType) : State()
        data class Content(
            val announcementIds: List<Long>,
            val courseNewsListItems: List<CourseNewsListItem.Data>,
            val sourceType: DataSourceType,
            val isLoadingRemote: Boolean,
            val isLoadingNextPage: Boolean
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val announcementIds: List<Long>) : Message()
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