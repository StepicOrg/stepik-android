package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mutate
import org.stepic.droid.util.plus
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_list.CourseListUserView
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

        val courseListItems = state.courseListDataItems.map {
            if (it.course.id == enrolledCourse.id) it.copy(
                course = enrolledCourse
            ) else it
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
            courseListItems = if (state.courseListItems.last() is CourseListItem.PlaceHolder) {
                courseListItems + CourseListItem.PlaceHolder
            } else {
                courseListItems
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

        val courseListItems = if (state.courseListItems.last() is CourseListItem.PlaceHolder) {
            courseListDataItems + CourseListItem.PlaceHolder
        } else {
            courseListDataItems
        }

        return CourseListView.State.Content(
            courseListDataItems = PagedList(
                courseListDataItems,
                state.courseListDataItems.page,
                state.courseListDataItems.hasNext,
                state.courseListDataItems.hasPrev
            ),
            courseListItems = courseListItems
        )
    }

    fun mapUserCourseRemoveState(oldState: CourseListUserView.State.Data, oldCourseListState: CourseListView.State.Content, courseId: Long): CourseListUserView.State.Data {
        val userCoursesUpdate = oldState.userCourses.mapNotNull {
            if (it.course == courseId) {
                null
            } else {
                it
            }
        }
        val index = oldCourseListState.courseListDataItems
            .indexOfFirst { it.course.id == courseId }

        val newItems = oldCourseListState.courseListDataItems.mutate { removeAt(index) }

        val courseListViewState = if (newItems.isNotEmpty()) {
            oldCourseListState.copy(
                courseListDataItems = newItems,
                courseListItems = newItems
            )
        } else {
            CourseListView.State.Empty
        }

        return oldState.copy(
            userCourses = userCoursesUpdate,
            courseListViewState = courseListViewState
        )
    }

    fun mapUserCourseAddState(
        oldState: CourseListUserView.State.Data,
        userCourse: UserCourse,
        courseListItem: CourseListItem.Data
    ): CourseListUserView.State.Data {
        val userCoursesUpdated = listOf(userCourse) + oldState.userCourses
        val courseListState = mapEnrolledCourseListItemState(oldState.courseListViewState, courseListItem)
        return oldState.copy(
            userCourses = userCoursesUpdated,
            courseListViewState = courseListState
        )
    }

    private fun mapEnrolledCourseListItemState(state: CourseListView.State, courseListItemEnrolled: CourseListItem.Data): CourseListView.State =
        when (state) {
            is CourseListView.State.Empty -> {
                CourseListView.State.Content(
                    courseListDataItems = PagedList(listOf(courseListItemEnrolled)),
                    courseListItems = listOf(courseListItemEnrolled)
                )
            }
            is CourseListView.State.Content -> {
                CourseListView.State.Content(
                    courseListDataItems = PagedList(
                        listOf(courseListItemEnrolled) + state.courseListDataItems,
                        state.courseListDataItems.page,
                        state.courseListDataItems.hasNext,
                        state.courseListDataItems.hasPrev
                    ),
                    courseListItems = listOf(courseListItemEnrolled) + state.courseListItems
                )
            }
            else ->
                state
        }
}