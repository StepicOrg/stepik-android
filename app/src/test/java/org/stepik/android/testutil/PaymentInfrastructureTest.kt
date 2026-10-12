package org.stepik.android.testutil

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.stepik.android.cache.course_purchase.BillingPurchasePayloadCacheDataSourceImpl
import org.stepik.android.domain.course_payments.model.CoursePayment

class PaymentInfrastructureTest {
    @Test
    fun finalAppCollaboratorAndSharedBillingFixturesWorkInAppSuite() {
        val payload = PaymentFixtures.payload()
        val cache = mock<BillingPurchasePayloadCacheDataSourceImpl>()
        whenever(cache.getPurchaseOrderPayload(any())) doReturn Single.just(payload)
        cache.getPurchaseOrderPayload(payload.orderId).test().assertResult(payload)
        assertEquals(BillingFixtures.PRODUCT_ID, BillingFixtures.product().productId)
    }

    @Test
    fun paymentFixturesSupportEveryStatusAndNullablePromo() {
        CoursePayment.Status.entries.forEach { status ->
            val payment = PaymentFixtures.payment(status = status, isPaid = false)
            assertEquals(status, payment.status)
            assertEquals(false, payment.isPaid)
            assertEquals(PaymentFixtures.COURSE_ID, payment.course)
            assertEquals(PaymentFixtures.USER_ID, payment.user)
        }
        assertEquals(null, PaymentFixtures.payload(promoCode = null).promoCode)
    }
}
