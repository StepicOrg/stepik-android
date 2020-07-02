package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.plus
import org.stepik.android.domain.course.mapper.CourseStatsMapper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.presentation.course_list.CourseListView
import javax.inject.Inject

class CourseListStateMapper
@Inject
constructor(
    private val courseStatsMapper: CourseStatsMapper
) {
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

    /**
     * Enrollment
     */
    fun mapToEnrollmentUpdateState(state: CourseListView.State, enrolledCourse: Course): CourseListView.State =
        mapCourseDataItems(state) { if (it.course.id == enrolledCourse.id) it.copy(course = enrolledCourse) else it }

    fun mapToEnrollmentUpdateState(state: CourseListView.State, enrolledCourse: CourseListItem.Data): CourseListView.State =
        mapCourseDataItems(state) { if (it.course.id == enrolledCourse.course.id) enrolledCourse else it }

    /**
     * User courses
     */
    fun mapToUserCourseUpdate(state: CourseListView.State, userCourse: UserCourse): CourseListView.State =
        mapCourseDataItems(state) { mergeCourseDataItemWithUserCourse(it, userCourse) }

    private fun mergeCourseDataItemWithUserCourse(item: CourseListItem.Data, userCourse: UserCourse): CourseListItem.Data =
        if (item.course.id == userCourse.course) {
            item.copy(courseStats = courseStatsMapper.mutateEnrolledState(item.courseStats) { copy(userCourse = userCourse) })
        } else {
            item
        }

    /**
     * Progress
     */
    fun mergeWithCourseProgress(state: CourseListView.State, progress: Progress): CourseListView.State =
        mapCourseDataItems(state) { mergeCourseDataItemWithProgress(it, progress) }

    private fun mergeCourseDataItemWithProgress(item: CourseListItem.Data, progress: Progress): CourseListItem.Data =
        if (item.course.progress == progress.id && progress.id != null) {
            item.copy(courseStats = item.courseStats.copy(progress = progress))
        } else {
            item
        }

    /**
     * Common
     */
    private inline fun mapCourseDataItems(state: CourseListView.State, transform: (CourseListItem.Data) -> CourseListItem.Data): CourseListView.State {
        if (state !is CourseListView.State.Content) {
            return state
        }

        val courseListItems = state.courseListItems.map { if (it is CourseListItem.Data) transform(it) else it }
        val courseListDataItems = state.courseListDataItems.mapPaged(transform)

        return state.copy(courseListDataItems, courseListItems)
    }
}