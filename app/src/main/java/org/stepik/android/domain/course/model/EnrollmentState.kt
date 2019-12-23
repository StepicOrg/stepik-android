package org.stepik.android.domain.course.model

import java.io.Serializable

sealed class EnrollmentState : Serializable {
    object Enrolled : EnrollmentState()
    object NotEnrolledFree : EnrollmentState()
//    data class NotEnrolledInApp(val skuWrapper: SkuSerializableWrapper) : EnrollmentState()
    object NotEnrolledWeb : EnrollmentState()
    object Pending : EnrollmentState()
}