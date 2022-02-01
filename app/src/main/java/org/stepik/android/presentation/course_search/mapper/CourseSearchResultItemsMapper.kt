package org.stepik.android.presentation.course_search.mapper

import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import ru.nobird.app.core.model.PagedList
import ru.nobird.app.core.model.transform
import javax.inject.Inject

class CourseSearchResultItemsMapper
@Inject
constructor() {
    fun updateCourseSearchResults(currentItems: PagedList<CourseSearchResultListItem.Data>, newItems: PagedList<CourseSearchResultListItem.Data>): PagedList<CourseSearchResultListItem.Data> {
        val newItemsMap = newItems.associateBy(CourseSearchResultListItem.Data::id)
        val updatedItems =
            currentItems
                .transform {
                    map { item ->
                        newItemsMap[item.id] ?: item
                    }
                }

        return updatedItems
    }
}