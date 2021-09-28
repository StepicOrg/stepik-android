package org.stepik.android.presentation.course_search.mapper

import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import org.stepik.android.presentation.course_search.CourseSearchFeature
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.core.model.concatWithPagedList
import ru.nobird.android.core.model.transform
import javax.inject.Inject

class CourseSearchStateMapper
@Inject
constructor() {
    fun concatCourseSearchResults(currentItems: PagedList<CourseSearchResultListItem.Data>, newItems: PagedList<CourseSearchResultListItem.Data>): CourseSearchFeature.State.Content {
        val updatedItems = currentItems.concatWithPagedList(newItems)
        return CourseSearchFeature.State.Content(updatedItems, updatedItems)
    }

    fun updateCourseSearchResults(currentItems: PagedList<CourseSearchResultListItem.Data>, newItems: PagedList<CourseSearchResultListItem.Data>): CourseSearchFeature.State.Content {
        val updatedItems =
            currentItems.transform {
                map { item ->
                    newItems
                        .find { courseSearchResultItem -> item.id == courseSearchResultItem.id }
                        ?: item
                }
            }
        return CourseSearchFeature.State.Content(updatedItems, updatedItems)
    }
}