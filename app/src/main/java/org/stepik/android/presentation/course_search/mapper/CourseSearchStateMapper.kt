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
    fun concatCourseSearchResults(currentState: CourseSearchFeature.State.Content, newItems: PagedList<CourseSearchResultListItem.Data>): CourseSearchFeature.State.Content {
        val updatedItems = currentState.courseSearchResultListDataItems.concatWithPagedList(newItems)
        return currentState.copy(courseSearchResultListDataItems = updatedItems)
    }

    fun updateCourseSearchResults(currentState: CourseSearchFeature.State.Content, newItems: PagedList<CourseSearchResultListItem.Data>): CourseSearchFeature.State.Content {
        val newItemsMap = newItems.associateBy(CourseSearchResultListItem.Data::id)
        val updatedItems =
            currentState
                .courseSearchResultListDataItems
                .transform {
                    map { item ->
                        newItemsMap[item.id] ?: item
                    }
                }

        return currentState.copy(courseSearchResultListDataItems = updatedItems, isLoadingNextPage = false)
    }
}