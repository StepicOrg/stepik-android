package org.stepik.android.domain.course_purchase.interactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.stepik.android.domain.course_payments.exception.CoursePurchaseVerificationException
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseTestFixture.Stage

@RunWith(Parameterized::class)
class CoursePurchaseVerificationStatusTest(private val status: CoursePayment.Status) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun statuses(): List<Array<Any>> = CoursePayment.Status.entries
            .filter { it != CoursePayment.Status.SUCCESS }.map { arrayOf<Any>(it) }
    }

    @Test fun nonSuccessVerificationRetainsPayloadAndSkipsConsumeDeletionAndEnrollment() {
        val f = CoursePurchaseTestFixture()
        try {
            val purchase = f.purchase()
            f.status = status
            f.consume(purchase).test().assertNotComplete().assertError(CoursePurchaseVerificationException::class.java)
            assertEquals(listOf(Stage.SAVE, Stage.VERIFY), f.subscribedStages())
            assertEquals(f.storedPayload(purchase), f.payloads[purchase.orderId])
            assertTrue(f.consumed.isEmpty())
            f.updateObserver.assertNoValues()
        } finally { f.close() }
    }

    @Test fun nonSuccessRestoreVerificationRetainsPayloadAndSkipsConsumeDeletionAndEnrollment() {
        val f = CoursePurchaseTestFixture()
        try {
            val purchase = f.purchase()
            f.status = status
            f.outstanding += purchase
            f.payloads[purchase.orderId!!] = f.storedPayload(purchase)
            f.interactor.restorePurchase(42).test().assertNotComplete().assertError(CoursePurchaseVerificationException::class.java)
            assertEquals(listOf(Stage.VERIFY), f.subscribedStages())
            assertEquals(f.storedPayload(purchase), f.payloads[purchase.orderId])
            assertTrue(f.consumed.isEmpty())
            f.updateObserver.assertNoValues()
        } finally { f.close() }
    }
}
