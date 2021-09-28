package org.stepik.android.domain.course_search.model

import ru.nobird.android.core.model.Identifiable

sealed class CourseSearchResultListItem {
    data class Data(val courseSearchResult: CourseSearchResult) : CourseSearchResultListItem(), Identifiable<Long> {
        override val id: Long =
            courseSearchResult.id
    }
    object Placeholder : CourseSearchResultListItem()
}