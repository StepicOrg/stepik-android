package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mutate
import org.stepic.droid.util.plus
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_list.CourseListView
import javax.inject.Inject

class CourseListStateMapper
@Inject
constructor() {
    fun mapToLoadMoreState(courseListState: CourseListView.State.Content): CourseListView.State =
        CourseListView.State.Content(
            courseListDataItems = courseListState.courseListDataItems,
            courseListItems = courseListState.courseListItems + CourseListItem.PlaceHolder
        )

    fun mapFromLoadMoreToSuccess(state: CourseListView.State, items: PagedList<CourseListItem.Data>): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        return CourseListView.State.Content(
            courseListDataItems = state.courseListDataItems + items,
            courseListItems = state.courseListItems.dropLastWhile(CourseListItem.PlaceHolder::equals) + items
        )
    }

    fun mapFromLoadMoreToError(state: CourseListView.State): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }
        return CourseListView.State.Content(
            courseListDataItems = state.courseListDataItems,
            courseListItems = state.courseListItems.dropLastWhile(CourseListItem.PlaceHolder::equals)
        )
    }

    fun mapToEnrollmentUpdateState(state: CourseListView.State, enrolledCourse: Course): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        return CourseListView.State.Content(
            courseListDataItems = PagedList(
                state.courseListDataItems.map {
                    if (it.course.id == enrolledCourse.id) it.copy(course = enrolledCourse) else it
                },
                state.courseListDataItems.page,
                state.courseListDataItems.hasNext,
                state.courseListDataItems.hasPrev
            ),
            courseListItems = state.courseListDataItems.map {
                if (it.course.id == enrolledCourse.id) it.copy(
                    course = enrolledCourse
                ) else it
            }
        )
    }

    fun mapToContinueCourseUpdateState(state: CourseListView.State, continuedCourse: Course): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val indexOf = state.courseListDataItems.indexOfFirst { it.id == continuedCourse.id }

        val courseListDataItems = state.courseListDataItems.mutate {
            val courseListItem = removeAt(indexOf)
            add(0, courseListItem)
        }

        return CourseListView.State.Content(
            courseListDataItems = PagedList(
                courseListDataItems,
                state.courseListDataItems.page,
                state.courseListDataItems.hasNext,
                state.courseListDataItems.hasPrev
            ),
            courseListItems = courseListDataItems
        )
    }

    fun mapEnrolledCourseListItemState(state: CourseListView.State, courseListItemEnrolled: CourseListItem.Data): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        return CourseListView.State.Content(
            courseListDataItems = PagedList(
                listOf(courseListItemEnrolled) + state.courseListDataItems,
                state.courseListDataItems.page,
                state.courseListDataItems.hasNext,
                state.courseListDataItems.hasPrev
            ),
            courseListItems = listOf(courseListItemEnrolled) + state.courseListItems
        )
    }
}