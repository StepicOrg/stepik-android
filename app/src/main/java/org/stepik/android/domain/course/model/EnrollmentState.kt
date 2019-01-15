package org.stepik.android.domain.course.model

import org.solovyev.android.checkout.Sku
import java.io.Serializable

sealed class EnrollmentState : Serializable {
    object Enrolled : EnrollmentState()
    object NotEnrolledFree : EnrollmentState()
    class NotEnrolledInApp(val sku: Sku) : EnrollmentState()
    object NotEnrolledWeb : EnrollmentState()
    object Pending : EnrollmentState()
}