package org.stepik.android.presentation.course.resolver

import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import javax.inject.Inject

class CoursePurchaseDataResolver
@Inject
constructor() {
    fun resolveCoursePurchaseData(courseHeaderData: CourseHeaderData): CoursePurchaseData? =
        (courseHeaderData.stats.enrollmentState as? EnrollmentState.NotEnrolledMobileTier)?.let { notEnrolledMobileTierState ->
            val promoCodeSku = when {
                courseHeaderData.deeplinkPromoCodeSku != PromoCodeSku.EMPTY ->
                    courseHeaderData.deeplinkPromoCodeSku

                notEnrolledMobileTierState.promoLightSku != null -> {
                    PromoCodeSku(
                        courseHeaderData.course.defaultPromoCodeName.orEmpty(),
                        notEnrolledMobileTierState.promoLightSku
                    )
                }

                else ->
                    PromoCodeSku.EMPTY
            }
            CoursePurchaseData(
                courseHeaderData.course,
                courseHeaderData.stats,
                notEnrolledMobileTierState.standardLightSku,
                promoCodeSku,
                courseHeaderData.course.isInWishlist
            )
        }
}