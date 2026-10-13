package org.stepik.android.remote.course_payments

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.*
import org.junit.Test
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.model.Meta
import org.stepik.android.remote.course_payments.model.CoursePaymentRequest
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse
import org.stepik.android.remote.course_payments.model.PromoCodeRequest
import org.stepik.android.remote.course_payments.model.PromoCodeResponse
import org.stepik.android.remote.course_payments.service.CoursePaymentService
import org.stepik.android.testutil.BillingFixtures

class CoursePaymentsRemoteDataSourceImplTest {
    private val service = mock<CoursePaymentService>()
    private val source = CoursePaymentsRemoteDataSourceImpl(service)
    private val courseId = 4_000_000_001L

    private fun payment(id: Long = 1, status: CoursePayment.Status = CoursePayment.Status.SUCCESS) =
        CoursePayment(
            id = id,
            course = courseId,
            isPaid = status == CoursePayment.Status.SUCCESS,
            status = status,
            user = 7_000_000_001L
        )

    private fun response(payments: List<CoursePayment>) = CoursePaymentsResponse(
        Meta(page = 1, hasNext = false, hasPrevious = false),
        coursePayments = payments
    )

    @Test fun createMapsEveryRequestFieldAndSelectsFirstResponseWithNullableOrPresentPromo() {
        for (promo in listOf(null, "", "Промо-🎓")) {
            val first = payment(101)
            whenever(service.createCoursePayment(any()))
                .thenReturn(Single.just(response(listOf(first, payment(102)))))
            val purchase = BillingFixtures.purchase(listOf("course.first", "course.second"))
            val product = BillingFixtures.product("different-inventory-id", currency = "JPY")
            source.createCoursePayment(courseId, sku = product, purchase, promoCode = promo).test().assertResult(first)
            val request = argumentCaptor<CoursePaymentRequest>()
            verify(service).createCoursePayment(request.capture())
            val body = request.firstValue.coursePayment
            assertEquals(courseId, body.course)
            assertEquals(CoursePaymentRequest.Body.Provider.GOOGLE, body.provider)
            assertEquals(promo, body.promoCode)
            assertEquals("synthetic-token", body.data.token)
            assertEquals("org.example.synthetic", body.data.packageName)
            assertEquals("course.first", body.data.productId)
            assertEquals("JPY", body.data.currency)
            assertEquals(12.34, body.data.amount.toDouble(), 0.000001)
            clearInvocations(service)
        }
    }

    @Test fun amountConvertsWholeFractionalZeroAndLargeMicrosWithFloatTolerance() {
        val amounts = listOf(25_000_000L, 12_345_678L, 0L, 123_456_789_012L)
        for (micros in amounts) {
            whenever(service.createCoursePayment(any()))
                .thenReturn(Single.just(response(listOf(payment()))))
            source.createCoursePayment(
                courseId,
                sku = BillingFixtures.product(micros = micros),
                purchase = BillingFixtures.purchase(),
                promoCode = null
            )
                .test()
                .assertValueCount(1)
                .assertComplete()
                .assertNoErrors()
            val request = argumentCaptor<CoursePaymentRequest>()
            verify(service).createCoursePayment(request.capture())
            val expected = micros / 1_000_000.0
            // At most two Float ULPs, with a one-micro minimum, for the existing Float contract.
            val tolerance = maxOf(0.000001, 2 * Math.ulp(expected.toFloat()).toDouble())
            assertEquals("micros=$micros", expected, request.firstValue.coursePayment.data.amount.toDouble(), tolerance)
            clearInvocations(service)
        }
    }

    @Test fun fetchForwardsCourseAndNullStatusReturnsAllResponseObjects() {
        val payments = CoursePayment.Status.entries.toTypedArray()
            .mapIndexed { index, status -> payment(index.toLong(), status) }
        whenever(service.getCoursePaymentsByCourseId(courseId))
            .thenReturn(Single.just(response(payments)))
        source.getCoursePaymentsByCourseId(courseId, null).test().assertResult(payments)
        verify(service).getCoursePaymentsByCourseId(courseId)
        verifyNoMoreInteractions(service)
    }

    @Test fun fetchEmptyResponseSucceedsWithOrWithoutStatus() {
        whenever(service.getCoursePaymentsByCourseId(courseId))
            .thenReturn(Single.just(response(emptyList())))
        source.getCoursePaymentsByCourseId(courseId, null)
            .test()
            .assertResult(emptyList())
        source.getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS)
            .test()
            .assertResult(emptyList())
    }

    @Test fun promoMapsRequestAndResponseAndPreservesOrdinaryEmptyAndUnicodeNames() {
        for (name in listOf("SYNTHETIC", "", "Учись-🎓")) {
            whenever(service.checkDeeplinkPromoCodeValidity(any()))
                .thenReturn(Single.just(PromoCodeResponse("1 234,56", "EUR")))
            source.checkDeeplinkPromoCodeValidity(courseId, name).test().assertResult(DeeplinkPromoCode(name, "1 234,56", "EUR"))
            val request = argumentCaptor<PromoCodeRequest>()
            verify(service).checkDeeplinkPromoCodeValidity(request.capture())
            assertEquals(courseId, request.firstValue.course)
            assertEquals(name, request.firstValue.name)
            clearInvocations(service)
        }
    }

    @Test fun originalServiceErrorsPropagateFromAllOperations() {
        val failure = IllegalStateException("synthetic service failure")
        whenever(service.createCoursePayment(any())).thenReturn(Single.error(failure))
        whenever(service.getCoursePaymentsByCourseId(courseId)).thenReturn(Single.error(failure))
        whenever(service.checkDeeplinkPromoCodeValidity(any())).thenReturn(Single.error(failure))
        source.createCoursePayment(courseId, BillingFixtures.product(), BillingFixtures.purchase(), null).test().assertNoValues().assertError { it === failure }
        source.getCoursePaymentsByCourseId(courseId, null).test().assertNoValues().assertError { it === failure }
        source.checkDeeplinkPromoCodeValidity(courseId, "SYNTHETIC").test().assertNoValues().assertError { it === failure }
    }

    @Test fun emptyCreateResponseFailsInsideStream() {
        whenever(service.createCoursePayment(any())).thenReturn(Single.just(response(emptyList())))
        source.createCoursePayment(courseId, BillingFixtures.product(), BillingFixtures.purchase(), null).test().assertNoValues().assertNotComplete().assertError(NoSuchElementException::class.java)
    }

    @Test fun missingOfferThrowsBeforeReturningStreamAndDoesNotCallService() {
        assertThrows(IllegalArgumentException::class.java) {
            source.createCoursePayment(courseId, BillingFixtures.product(hasOffer = false), BillingFixtures.purchase(), null)
        }
        org.mockito.Mockito.verifyNoInteractions(service)
    }

    @Test fun emptyPurchaseProductsThrowBeforeReturningStreamAndDoNotCallService() {
        assertThrows(NoSuchElementException::class.java) {
            source.createCoursePayment(courseId, BillingFixtures.product(), BillingFixtures.purchase(emptyList()), null)
        }
        org.mockito.Mockito.verifyNoInteractions(service)
    }

    @Test fun disposingCreateBeforeServiceEmissionRemovesSubscriptionAndSuppressesDelivery() {
        val pending = SingleSubject.create<CoursePaymentsResponse>()
        whenever(service.createCoursePayment(any())).thenReturn(pending)
        val observer = source.createCoursePayment(courseId, BillingFixtures.product(), BillingFixtures.purchase(), null).test()
        assertTrue(pending.hasObservers())
        observer.assertNoValues().assertNotComplete()
        observer.dispose()
        assertFalse(pending.hasObservers())
        pending.onSuccess(response(listOf(payment())))
        observer.assertNoValues().assertNoErrors().assertNotComplete()
    }

    @Test fun disposingFetchBeforeServiceEmissionSuppressesDelivery() {
        val pending = SingleSubject.create<CoursePaymentsResponse>()
        whenever(service.getCoursePaymentsByCourseId(courseId)).thenReturn(pending)
        val observer = source.getCoursePaymentsByCourseId(courseId, null).test()
        observer.dispose()
        assertFalse(pending.hasObservers())
        pending.onSuccess(response(listOf(payment())))
        observer.assertNoValues().assertNoErrors().assertNotComplete()
    }

    @Test fun disposingPromoBeforeServiceErrorSuppressesDelivery() {
        val pending = SingleSubject.create<PromoCodeResponse>()
        whenever(service.checkDeeplinkPromoCodeValidity(any())).thenReturn(pending)
        val observer = source.checkDeeplinkPromoCodeValidity(courseId, "SYNTHETIC").test()
        observer.dispose()
        assertFalse(pending.hasObservers())
        pending.onError(IllegalStateException("synthetic disposed service failure"))
        observer.assertNoValues().assertNoErrors().assertNotComplete()
    }
}
