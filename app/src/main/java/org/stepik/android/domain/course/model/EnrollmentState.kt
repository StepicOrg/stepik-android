package org.stepik.android.domain.course.model

import org.stepik.android.domain.user_courses.model.UserCourse
import java.io.Serializable

sealed class EnrollmentState : Serializable {
    data class Enrolled(val userCourse: UserCourse) : EnrollmentState()
    object NotEnrolledFree : EnrollmentState()
//    data class NotEnrolledInApp(val skuWrapper: SkuSerializableWrapper) : EnrollmentState()
    object NotEnrolledWeb : EnrollmentState()
    object Pending : EnrollmentState()
}