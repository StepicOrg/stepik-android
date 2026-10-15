package org.stepik.android.view.debug.ui

import android.content.Context
import org.stepic.droid.R
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

object CoursePurchasePreviewData {
    fun create(context: Context): CoursePurchaseData {
        val price = context.getString(R.string.debug_course_purchase_preview_price)
        val sku = LightSku(id = "debug-preview-sku", price = price)
        val course = Course(
            id = 0L,
            title = context.getString(R.string.debug_course_purchase_preview_title),
            displayPrice = price,
            isPaid = true
        )
        val stats = CourseStats(
            review = 0.0,
            learnersCount = 0,
            readiness = 0.0,
            progress = null,
            enrollmentState = EnrollmentState.NotEnrolledMobileTier(sku, null)
        )
        return CoursePurchaseData(course, stats, sku, PromoCodeSku.EMPTY, isWishlisted = false, purchaseState = -1)
    }
}
