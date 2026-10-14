package org.stepik.android.data.course_payments.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.course_payments.CoursePaymentsCacheDataSourceImpl
import org.stepik.android.cache.testutil.BillingStorageFixtures.payment
import org.stepik.android.cache.testutil.LegacyPaymentsDatabase
import org.stepik.android.cache.testutil.assertPayments
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitError
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode

@RunWith(AndroidJUnit4::class)
class CoursePaymentsRepositoryPersistenceTest {
    @Test fun remoteReadThroughReplacesSqlRowsAndCacheReadsStayOffline() {
        LegacyPaymentsDatabase().use { database ->
            val cache = CoursePaymentsCacheDataSourceImpl(database.dao)
            val original = payment(id = 1, status = CoursePayment.Status.PENDING, isPaid = false)
            val unrelated = payment(id = 2, course = 99)
            cache.saveCoursePayments(listOf(original, unrelated)).awaitComplete()
            val refreshed = payment(id = original.id, user = 88)
            val remote = FakePaymentsRemote(listOf(refreshed))
            val repository = CoursePaymentsRepositoryImpl(remote, cache)
            assertPayments(listOf(refreshed), repository.getCoursePaymentsByCourseId(original.course, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE).awaitValue())
            assertEquals(listOf(original.course to CoursePayment.Status.SUCCESS), remote.requests)
            remote.fail = true
            // New wrapper/repository instances still read real SQL, without a remote subscription.
            val offline = CoursePaymentsRepositoryImpl(remote, CoursePaymentsCacheDataSourceImpl(database.dao))
            assertPayments(listOf(refreshed), offline.getCoursePaymentsByCourseId(original.course, null, DataSourceType.CACHE).awaitValue())
            assertPayments(emptyList(), offline.getCoursePaymentsByCourseId(original.course, CoursePayment.Status.PENDING, DataSourceType.CACHE).awaitValue())
            assertPayments(listOf(unrelated), offline.getCoursePaymentsByCourseId(unrelated.course, null, DataSourceType.CACHE).awaitValue())
            assertEquals(1, remote.requests.size)
            offline.getCoursePaymentsByCourseId(original.course, null, DataSourceType.REMOTE).awaitError(IllegalStateException::class.java)
            assertPayments(listOf(refreshed), offline.getCoursePaymentsByCourseId(original.course, null, DataSourceType.CACHE).awaitValue())
            assertEquals(2, remote.requests.size)
        }
    }

    private class FakePaymentsRemote(private val items: List<CoursePayment>) : CoursePaymentsRemoteDataSource {
        val requests = mutableListOf<Pair<Long, CoursePayment.Status?>>()
        var fail = false
        override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?) = Single.defer {
            requests += courseId to coursePaymentStatus
            if (fail) Single.error(IllegalStateException("synthetic offline")) else Single.just(items)
        }
        override fun createCoursePayment(courseId: Long, sku: ProductDetails, purchase: Purchase, promoCode: String?): Single<CoursePayment> =
            Single.error(AssertionError("unexpected create"))
        override fun checkDeeplinkPromoCodeValidity(courseId: Long, name: String): Single<DeeplinkPromoCode> =
            Single.error(AssertionError("unexpected promo"))
    }
}
