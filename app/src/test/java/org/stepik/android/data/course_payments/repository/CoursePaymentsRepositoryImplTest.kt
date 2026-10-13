package org.stepik.android.data.course_payments.repository

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.testutil.BillingFixtures

class CoursePaymentsRepositoryImplTest {
    private val remote = mock<CoursePaymentsRemoteDataSource>()
    private val cache = mock<CoursePaymentsCacheDataSource>()
    private val repository = CoursePaymentsRepositoryImpl(remote, cache)
    private val payment = CoursePayment(101, 42, true, CoursePayment.Status.SUCCESS, 71)
    private val failure = IllegalStateException("synthetic failure")

    @Test fun remoteWaitsForFetchAndCacheCompletionBeforeEmitting() {
        val fetch = SingleSubject.create<List<CoursePayment>>()
        val save = CompletableSubject.create()
        val events = mutableListOf<String>()
        whenever(remote.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS))
            .thenReturn(Single.defer { events += "fetch"; fetch })
        whenever(cache.saveCoursePayments(listOf(payment)))
            .thenReturn(Completable.defer { events += "save"; save })
        val stream = repository.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE)
        assertTrue(events.isEmpty())
        val observer = stream.test()
        try {
            assertEquals(listOf("fetch"), events)
            verifyNoInteractions(cache)
            fetch.onSuccess(listOf(payment))
            assertEquals(listOf("fetch", "save"), events)
            observer.assertNoValues().assertNotComplete()
            save.onComplete()
            observer.assertResult(listOf(payment))
        } finally { observer.dispose() }
    }

    @Test fun remoteFailurePropagatesWithoutCacheFallbackOrWrite() {
        whenever(remote.getCoursePaymentsByCourseId(42, null)).thenReturn(Single.error(failure))
        repository.getCoursePaymentsByCourseId(42, null, DataSourceType.REMOTE).test()
            .assertNoValues().assertError { it === failure }
        verifyNoInteractions(cache)
    }

    @Test fun cacheWriteFailurePropagatesWithoutCacheRead() {
        whenever(remote.getCoursePaymentsByCourseId(42, null)).thenReturn(Single.just(listOf(payment)))
        whenever(cache.saveCoursePayments(listOf(payment))).thenReturn(Completable.error(failure))
        repository.getCoursePaymentsByCourseId(42, null, DataSourceType.REMOTE).test()
            .assertNoValues().assertError { it === failure }
        verify(cache).saveCoursePayments(listOf(payment))
        verifyNoMoreInteractions(cache)
    }

    @Test fun cacheRoutesCourseAndStatusWithoutRemoteAndPropagatesReadErrors() {
        whenever(cache.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS)).thenReturn(Single.just(listOf(payment)))
        whenever(cache.getCoursePaymentsByCourseId(42, null)).thenReturn(Single.error(failure))
        repository.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS, DataSourceType.CACHE).test().assertResult(listOf(payment))
        repository.getCoursePaymentsByCourseId(42, null, DataSourceType.CACHE).test().assertNoValues().assertError { it === failure }
        verifyNoInteractions(remote)
    }

    @Test fun createForwardsAllArgumentsWithoutSavingResponse() {
        val product = BillingFixtures.product()
        val purchase = BillingFixtures.purchase()
        for (promo in listOf(null, "", "ПРОМО")) {
            whenever(remote.createCoursePayment(42, product, purchase, promo)).thenReturn(Single.just(payment))
            repository.createCoursePayment(42, product, purchase, promo).test().assertResult(payment)
            verify(remote).createCoursePayment(42, product, purchase, promo)
        }
        verifyNoInteractions(cache)
    }

    @Test fun createErrorIsPreservedWithoutCacheWrites() {
        val product = BillingFixtures.product()
        val purchase = BillingFixtures.purchase()
        whenever(remote.createCoursePayment(42, product, purchase, null)).thenReturn(Single.error(failure))
        repository.createCoursePayment(42, product, purchase, null).test().assertNoValues().assertError { it === failure }
        verifyNoInteractions(cache)
    }

    @Test fun promoPreservesSuccessAndConvertsDifferentErrorsToEmpty() {
        val promo = DeeplinkPromoCode("ПРОМО", "12,34", "EUR")
        whenever(remote.checkDeeplinkPromoCodeValidity(42, promo.name)).thenReturn(Single.just(promo))
        repository.checkDeeplinkPromoCodeValidity(42, promo.name).test().assertResult(promo)
        for (error in listOf(failure, IllegalArgumentException("invalid"))) {
            whenever(remote.checkDeeplinkPromoCodeValidity(42, "bad")).thenReturn(Single.error(error))
            repository.checkDeeplinkPromoCodeValidity(42, "bad").test().assertResult(DeeplinkPromoCode.EMPTY)
        }
        verifyNoInteractions(cache)
    }
}
