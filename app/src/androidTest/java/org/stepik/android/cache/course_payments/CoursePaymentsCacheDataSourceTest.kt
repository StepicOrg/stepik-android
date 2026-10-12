package org.stepik.android.cache.course_payments

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.testutil.BillingStorageFixtures.payment
import org.stepik.android.cache.testutil.LegacyPaymentsDatabase
import org.stepik.android.cache.testutil.assertPayments
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitError
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.domain.course_payments.model.CoursePayment

@RunWith(AndroidJUnit4::class)
class CoursePaymentsCacheDataSourceTest {
    private lateinit var database: LegacyPaymentsDatabase
    private lateinit var cache: CoursePaymentsCacheDataSourceImpl

    @Before
    fun openDatabase() {
        database = LegacyPaymentsDatabase()
        cache = CoursePaymentsCacheDataSourceImpl(database.dao)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun nullStatusReadsAllStatusesAndSuccessReadsOnlySuccessfulRows() {
        val items = CoursePayment.Status.values().mapIndexed { index, status -> payment(id = index.toLong(), status = status) }
        val other = payment(id = 99L, course = 99L)
        cache.saveCoursePayments(items + other).awaitComplete()
        assertPayments(items, cache.getCoursePaymentsByCourseId(items.first().course, null).awaitValue())
        assertPayments(items.filter { it.status == CoursePayment.Status.SUCCESS },
            cache.getCoursePaymentsByCourseId(items.first().course, CoursePayment.Status.SUCCESS).awaitValue())
        assertPayments(emptyList(), cache.getCoursePaymentsByCourseId(-1L, null).awaitValue())
        assertPayments(emptyList(), cache.getCoursePaymentsByCourseId(-1L, CoursePayment.Status.SUCCESS).awaitValue())
        assertPayments(listOf(other), cache.getCoursePaymentsByCourseId(other.course, null).awaitValue())
    }

    @Test
    fun emptySavePreservesExistingRows() {
        val item = payment()
        cache.saveCoursePayments(listOf(item)).awaitComplete()
        cache.saveCoursePayments(emptyList()).awaitComplete()
        assertPayments(listOf(item), cache.getCoursePaymentsByCourseId(item.course, null).awaitValue())
    }

    @Test
    fun subsetRefreshReplacesMatchingPaymentAndPreservesSameAndOtherCourseRows() {
        val first = payment(id = 1L, status = CoursePayment.Status.PENDING, isPaid = false)
        val sameCourse = payment(id = 2L)
        val otherCourse = payment(id = 3L, course = 33L)
        cache.saveCoursePayments(listOf(first, sameCourse, otherCourse)).awaitComplete()
        val refreshed = payment(id = first.id, user = 88L)
        cache.saveCoursePayments(listOf(refreshed)).awaitComplete()
        assertPayments(listOf(refreshed, sameCourse), cache.getCoursePaymentsByCourseId(first.course, null).awaitValue())
        assertPayments(listOf(otherCourse), cache.getCoursePaymentsByCourseId(otherCourse.course, null).awaitValue())
    }

    @Test
    fun failedBatchRollsBackPriorReplacementAndInsertionAndAllowsNextWrite() {
        val original = payment(id = 1L, status = CoursePayment.Status.PENDING, isPaid = false)
        val unrelated = payment(id = 2L, course = 33L)
        cache.saveCoursePayments(listOf(original, unrelated)).awaitComplete()
        database.sql.execSQL("""
            CREATE TEMP TRIGGER fail_test_payment BEFORE INSERT ON course_payments
            WHEN NEW.id = 999
            BEGIN SELECT RAISE(ABORT, 'synthetic payment insert failure'); END
        """.trimIndent())
        cache.saveCoursePayments(listOf(payment(id = original.id), payment(id = 3L), payment(id = 999L)))
            .awaitError(SQLiteConstraintException::class.java)
        assertFalse(database.sql.inTransaction())
        assertPayments(listOf(original), cache.getCoursePaymentsByCourseId(original.course, null).awaitValue())
        assertPayments(listOf(unrelated), cache.getCoursePaymentsByCourseId(unrelated.course, null).awaitValue())
        database.sql.execSQL("DROP TRIGGER fail_test_payment")
        val next = payment(id = 4L)
        cache.saveCoursePayments(listOf(next)).awaitComplete()
        assertPayments(listOf(original, next), cache.getCoursePaymentsByCourseId(original.course, null).awaitValue())
    }
}
