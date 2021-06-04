package org.stepik.android.domain.course.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course
import org.stepik.android.presentation.user_courses.model.UserCourseAction

class UserCourseActionEvent(
    userCourseAction: UserCourseAction,
    course: Course,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        /**
         * User course action
         */
        private const val PARAM_ACTION = "action"

        private const val VALUE_FAVORITE_ADD = "favorite_add"
        private const val VALUE_FAVORITE_REMOVE = "favorite_remove"
        private const val VALUE_ARCHIVE_ADD = "archive_add"
        private const val VALUE_ARCHIVE_REMOVE = "archive_remove"

        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_IS_PAID = "is_paid"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "User course action"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_ACTION to mapToUserCourseActionValue(userCourseAction),
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            PARAM_IS_PAID to course.isPaid,
            PARAM_SOURCE to source.name
        ) + source.params.mapKeys { "${PARAM_SOURCE}_${it.key}" }

    private fun mapToUserCourseActionValue(userCourseAction: UserCourseAction) =
        when (userCourseAction) {
            UserCourseAction.ADD_FAVORITE ->
                VALUE_FAVORITE_ADD
            UserCourseAction.REMOVE_FAVORITE ->
                VALUE_FAVORITE_REMOVE
            UserCourseAction.ADD_ARCHIVE ->
                VALUE_ARCHIVE_ADD
            UserCourseAction.REMOVE_ARCHIVE ->
                VALUE_ARCHIVE_REMOVE
        }
}