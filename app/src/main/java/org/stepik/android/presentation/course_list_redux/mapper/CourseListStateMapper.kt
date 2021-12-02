package org.stepik.android.presentation.course_list_redux.mapper

import ru.nobird.android.core.model.mapPaged
import org.stepik.android.domain.course.mapper.CourseStatsMapper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.wishlist.model.WishlistAction
import javax.inject.Inject

/***
 *  Similar to org.stepik.android.presentation.course_list.mapper.CourseListStateMapper
 *  Created due to transferring course lists to redux arch
 */
class CourseListStateMapper
@Inject
constructor(
    private val courseStatsMapper: CourseStatsMapper
) {
    /**
     * Enrollment
     */
    fun mapToEnrollmentUpdateState(state: CourseListFeature.State, enrolledCourse: Course): CourseListFeature.State =
        mapCourseDataItems(state) { if (it.course.id == enrolledCourse.id) it.copy(course = enrolledCourse) else it }

    fun mapToEnrollmentUpdateState(state: CourseListFeature.State, enrolledCourse: CourseListItem.Data): CourseListFeature.State =
        mapCourseDataItems(state) { if (it.course.id == enrolledCourse.course.id) enrolledCourse else it }

    /**
     * User courses
     */
    fun mapToUserCourseUpdate(state: CourseListFeature.State, userCourse: UserCourse): CourseListFeature.State =
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
    fun mergeWithCourseProgress(state: CourseListFeature.State, progress: Progress): CourseListFeature.State =
        mapCourseDataItems(state) { mergeCourseDataItemWithProgress(it, progress) }

    private fun mergeCourseDataItemWithProgress(item: CourseListItem.Data, progress: Progress): CourseListItem.Data =
        if (item.course.progress == progress.id && progress.id != null) {
            item.copy(courseStats = item.courseStats.copy(progress = progress))
        } else {
            item
        }

    /**
     * Wishlist
     */
    fun mapToWishlistUpdate(state: CourseListFeature.State, wishlistOperationData: WishlistOperationData): CourseListFeature.State =
        mapCourseDataItems(state) { mergeCourseDataItemWithWishlist(it, wishlistOperationData) }

    private fun mergeCourseDataItemWithWishlist(item: CourseListItem.Data, wishlistOperationData: WishlistOperationData): CourseListItem.Data =
        if (item.course.id == wishlistOperationData.courseId) {
            item.copy(course = item.course.copy(isInWishlist = wishlistOperationData.wishlistAction == WishlistAction.ADD))
        } else {
            item
        }

    /**
     * Common
     */
    private inline fun mapCourseDataItems(state: CourseListFeature.State, transform: (CourseListItem.Data) -> CourseListItem.Data): CourseListFeature.State {
        if (state !is CourseListFeature.State.Content) {
            return state
        }

        val courseListItems = state.courseListItems.map { if (it is CourseListItem.Data) transform(it) else it }
        val courseListDataItems = state.courseListDataItems.mapPaged(transform)

        return state.copy(courseListDataItems, courseListItems)
    }
}