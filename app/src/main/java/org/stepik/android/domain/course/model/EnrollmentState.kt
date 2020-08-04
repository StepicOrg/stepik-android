package org.stepik.android.domain.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.billing.model.SkuSerializableWrapper
import org.stepik.android.domain.user_courses.model.UserCourse

sealed class EnrollmentState : Parcelable {
    @Parcelize
    data class Enrolled(val userCourse: UserCourse, val isUserCourseUpdating: Boolean = false) : EnrollmentState()

    @Parcelize
    object NotEnrolledFree : EnrollmentState()

    @Parcelize
    data class NotEnrolledInApp(val skuWrapper: SkuSerializableWrapper) : EnrollmentState()

    @Parcelize
    object NotEnrolledWeb : EnrollmentState()

    @Parcelize
    object Pending : EnrollmentState()
}