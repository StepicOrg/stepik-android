package org.stepik.android.presentation.course.resolver

import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_payments.model.CoursePurchaseInfo
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import javax.inject.Inject

class CoursePurchaseDataResolver
@Inject
constructor() {
    fun resolveCoursePurchaseData(courseHeaderData: CourseHeaderData): CoursePurchaseData? =
        (courseHeaderData.stats.enrollmentState as? EnrollmentState.NotEnrolledMobileTier)
            ?.let { notEnrolledMobileTierState ->
                resolveCoursePurchaseData(
                    courseHeaderData.course,
                    courseHeaderData.stats,
                    notEnrolledMobileTierState,
                    courseHeaderData.deeplinkPromoCodeSku,
                    (courseHeaderData.coursePurchaseInfo as? CoursePurchaseInfo.Result)?.purchaseState ?: -1
                )
        }

    fun resolveCoursePurchaseData(
        course: Course,
        stats: CourseStats,
        notEnrolledMobileTierState: EnrollmentState.NotEnrolledMobileTier,
        deeplinkPromoCodeSku: PromoCodeSku,
        purchaseState: Int
    ): CoursePurchaseData {
        val promoCodeSku = when {
            deeplinkPromoCodeSku != PromoCodeSku.EMPTY ->
                deeplinkPromoCodeSku

            notEnrolledMobileTierState.promoLightSku != null -> {
                PromoCodeSku(
                    course.defaultPromoCodeName.orEmpty(),
                    notEnrolledMobileTierState.promoLightSku
                )
            }

            else ->
                PromoCodeSku.EMPTY
        }
        return CoursePurchaseData(
            course,
            stats,
            notEnrolledMobileTierState.standardLightSku,
            promoCodeSku,
            course.isInWishlist,
            purchaseState
        )
    }
}