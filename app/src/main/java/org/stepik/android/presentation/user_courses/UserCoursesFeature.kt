package org.stepik.android.presentation.user_courses

import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesFeature {
    sealed class Message {
        data class UserCourseOperationUpdate(val userCourse: UserCourse) : Message()
    }
    sealed class Action
}