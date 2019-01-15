package org.stepik.android.domain.course.model

import org.solovyev.android.checkout.Sku

sealed class EnrollmentState {
    object Enrolled : EnrollmentState()
    object NotEnrolledFree : EnrollmentState()
    class NotEnrolledInApp(val sku: Sku) : EnrollmentState()
    object NotEnrolledWeb : EnrollmentState()
    object Pending : EnrollmentState()
}