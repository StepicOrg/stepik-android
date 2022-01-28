package org.stepik.android.presentation.course_news

import org.stepik.android.domain.announcement.model.Announcement
import ru.nobird.android.core.model.PagedList

interface CourseNewsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val announcements: PagedList<Announcement>) : State()
    }

    sealed class Message

    sealed class Action {
        sealed class ViewAction : Action()
    }
}