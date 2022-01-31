package org.stepik.android.domain.course_news.model

import org.stepik.android.domain.announcement.model.Announcement
import ru.nobird.android.core.model.Identifiable

sealed class CourseNewsListItem {
    object Placeholder : CourseNewsListItem()
    data class Data(val announcement: Announcement) : CourseNewsListItem(), Identifiable<Long> {
        override val id: Long =
            announcement.id
    }
}