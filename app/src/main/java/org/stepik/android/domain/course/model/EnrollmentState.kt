package org.stepik.android.domain.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.user_courses.model.UserCourse

sealed class EnrollmentState : Parcelable {
    @Parcelize
    data class Enrolled(val userCourse: UserCourse, val isUserCourseUpdating: Boolean = false) : EnrollmentState()

    @Parcelize
    object NotEnrolledFree : EnrollmentState()

    @Parcelize
    data class NotEnrolledMobileTier(val standardLightSku: LightSku, val promoLightSku: LightSku?) : EnrollmentState()

    @Parcelize
    object NotEnrolledWeb : EnrollmentState()

    /**
     * Course cannot be purchased via IAP
     */
    @Parcelize
    object NotEnrolledUnavailableIAP : EnrollmentState()

    @Parcelize
    object NotEnrolledEnded : EnrollmentState()

    @Parcelize
    object NotEnrolledCantBeBought : EnrollmentState()

    @Parcelize
    object Pending : EnrollmentState()
}