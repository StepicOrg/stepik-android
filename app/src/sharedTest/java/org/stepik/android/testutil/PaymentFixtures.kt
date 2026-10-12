package org.stepik.android.testutil

import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

/** Pure models shared by JVM and device tests; never use real customer/payment values. */
object PaymentFixtures {
    const val COURSE_ID = 101L
    const val USER_ID = 202L
    const val PROMO_CODE = "TEST-PROMO"

    fun payment(
        id: Long = 303L,
        course: Long = COURSE_ID,
        user: Long = USER_ID,
        status: CoursePayment.Status = CoursePayment.Status.SUCCESS,
        isPaid: Boolean = true
    ) = CoursePayment(id = id, course = course, isPaid = isPaid, status = status, user = user)

    fun payload(
        orderId: String = "test-order-101",
        courseId: Long = COURSE_ID,
        profileId: Long = USER_ID,
        accountId: String = "test-account-hash",
        courseHash: String = "test-course-hash",
        promoCode: String? = PROMO_CODE
    ) = BillingPurchasePayload(
        orderId = orderId,
        courseId = courseId,
        profileId = profileId,
        obfuscatedAccountId = accountId,
        obfuscatedProfileId = courseHash,
        promoCode = promoCode
    )
}
