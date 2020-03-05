package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.plus
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_list.CourseListView
import javax.inject.Inject

class CourseListStateMapper
@Inject
constructor() {
    fun mapToLoadMoreState(courseListState: CourseListView.State.Content): CourseListView.State =
        courseListState.copy(courseListItems = courseListState.courseListItems.plus(CourseListItem.PlaceHolder))

    fun mapFromLoadMoreToSuccess(state: CourseListView.State, items: PagedList<CourseListItem.Data>): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val oldItems = state.courseListItems.dropLastWhile(CourseListItem.PlaceHolder::equals)

        return state.copy(
            courseListItems = PagedList(
                list    = oldItems + items,
                page    = items.page,
                hasNext = items.hasNext,
                hasPrev = items.hasPrev)
        )
    }

    fun mapFromLoadMoreToError(state: CourseListView.State): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val pagedList = state.courseListItems

        return state.copy(
            courseListItems = PagedList(
                list    = pagedList.dropWhile(CourseListItem.PlaceHolder::equals),
                page    = pagedList.page,
                hasNext = pagedList.hasNext,
                hasPrev = pagedList.hasPrev)
        )
    }
}