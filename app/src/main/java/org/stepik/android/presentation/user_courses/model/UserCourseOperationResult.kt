package org.stepik.android.presentation.user_courses.model

import org.stepik.android.domain.user_courses.model.UserCourse

data class UserCourseOperationResult(val userCourse: UserCourse, val userCourseAction: UserCourseAction)