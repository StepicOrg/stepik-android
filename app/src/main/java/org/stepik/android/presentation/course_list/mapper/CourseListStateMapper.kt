package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.plus
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.presentation.course_list.CourseListView
import javax.inject.Inject

class CourseListStateMapper
@Inject
constructor() {
    fun mapToLoadMoreState(courseListState: CourseListView.State.Content): CourseListView.State =
        CourseListView.State.Content(
            courseListDataItems = courseListState.courseListDataItems,
            courseListItems = courseListState.courseListItems + CourseListItem.PlaceHolder()
        )

    fun mapFromLoadMoreToSuccess(state: CourseListView.State, items: PagedList<CourseListItem.Data>): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        return CourseListView.State.Content(
            courseListDataItems = state.courseListDataItems + items,
            courseListItems = state.courseListItems.dropLastWhile { it is CourseListItem.PlaceHolder } + items
        )
    }

    fun mergeWithUpdatedItems(state: CourseListView.State, itemsMap: Map<Long, CourseListItem.Data>): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        return state
            .copy(
                state.courseListDataItems.mapPaged { item -> itemsMap[item.course.id] ?: item },
                state.courseListItems.map { item ->
                    if (item is CourseListItem.Data) {
                        itemsMap[item.course.id] ?: item
                    } else {
                        item
                    }
                }
            )
    }

    fun mapFromLoadMoreToError(state: CourseListView.State): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }
        return CourseListView.State.Content(
            courseListDataItems = state.courseListDataItems,
            courseListItems = state.courseListItems.dropLastWhile { it is CourseListItem.PlaceHolder }
        )
    }

    fun mapToEnrollmentUpdateState(state: CourseListView.State, enrolledCourse: Course): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val courseListItems =
            state.courseListDataItems.mapPaged {
                if (it.course.id == enrolledCourse.id) it.copy(course = enrolledCourse) else it
            }

        return CourseListView.State.Content(
            courseListDataItems = courseListItems,
            courseListItems =
                if (state.courseListItems.last() is CourseListItem.PlaceHolder) {
                    courseListItems + CourseListItem.PlaceHolder()
                } else {
                    courseListItems
                }
        )
    }

    fun mergeWithCourseProgress(state: CourseListView.State, progress: Progress): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val courseListItems = state.courseListItems.map { mergeCourseItemWithProgress(it, progress) }
        val courseListDataItems = state.courseListDataItems.mapPaged { mergeCourseDataItemWithProgress(it, progress) }

        return state.copy(courseListDataItems, courseListItems)
    }

    private fun mergeCourseItemWithProgress(item: CourseListItem, progress: Progress): CourseListItem =
        when (item) {
            is CourseListItem.PlaceHolder ->
                item

            is CourseListItem.Data ->
                mergeCourseDataItemWithProgress(item, progress)
        }

    private fun mergeCourseDataItemWithProgress(item: CourseListItem.Data, progress: Progress): CourseListItem.Data =
        if (item.course.progress == progress.id && progress.id != null) {
            item.copy(courseStats = item.courseStats.copy(progress = progress))
        } else {
            item
        }
}