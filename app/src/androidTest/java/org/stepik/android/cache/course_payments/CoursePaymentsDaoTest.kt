package org.stepik.android.cache.course_payments

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments.Columns
import org.stepik.android.cache.testutil.BillingStorageFixtures.payment
import org.stepik.android.cache.testutil.LegacyPaymentsDatabase
import org.stepik.android.cache.testutil.assertPayments
import org.stepik.android.domain.course_payments.model.CoursePayment

@RunWith(AndroidJUnit4::class)
class CoursePaymentsDaoTest {
    private lateinit var database: LegacyPaymentsDatabase

    @Before
    fun openDatabase() {
        database = LegacyPaymentsDatabase()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun roundTripsLongIdsBothBooleansAndEveryStatusWithStableOrdinals() {
        val statuses = listOf(
            CoursePayment.Status.PENDING, CoursePayment.Status.AMOUNT_BLOCKED, CoursePayment.Status.SUCCESS,
            CoursePayment.Status.CANCELED, CoursePayment.Status.FAILED, CoursePayment.Status.EXPIRED
        )
        assertEquals(statuses, CoursePayment.Status.values().toList())
        val items = statuses.flatMapIndexed { index, status ->
            listOf(
                payment(id = 3_000_000_000L + index * 2, isPaid = false, status = status),
                payment(id = 3_000_000_001L + index * 2, isPaid = true, status = status)
            )
        }
        database.dao.insertOrReplaceAll(items)
        assertPayments(items, database.dao.getAll())
        database.sql.query("SELECT id, is_paid, status FROM course_payments").use { cursor ->
            val byId = items.associateBy { it.id }
            while (cursor.moveToNext()) {
                val expected = byId.getValue(cursor.getLong(0))
                assertEquals(if (expected.isPaid) 1 else 0, cursor.getInt(1))
                assertEquals(statuses.indexOf(expected.status), cursor.getInt(2))
            }
        }
    }

    @Test
    fun replacementByPaymentIdUpdatesAllFieldsAndPreservesOtherRows() {
        val original = payment()
        val other = payment(id = 12L)
        val replacement = payment(id = original.id, course = 77L, isPaid = false, status = CoursePayment.Status.FAILED, user = 88L)
        database.dao.insertOrReplaceAll(listOf(original, other))
        database.dao.insertOrReplace(replacement)
        assertPayments(listOf(replacement, other), database.dao.getAll())
    }

    @Test
    fun bulkInsertionFiltersByCourseAndOptionalStatusWithoutCrossCourseLeaks() {
        val firstCourse = 4_000_000_001L
        val items = CoursePayment.Status.values().mapIndexed { index, status ->
            payment(id = index.toLong(), course = firstCourse, status = status)
        }
        val other = payment(id = 99L, course = firstCourse + 1)
        database.dao.insertOrReplaceAll(items + other)
        assertPayments(items, database.dao.getAll(mapOf(Columns.COURSE to firstCourse.toString())))
        CoursePayment.Status.values().forEach { status ->
            assertPayments(items.filter { it.status == status }, database.dao.getAll(mapOf(
                Columns.COURSE to firstCourse.toString(), Columns.STATUS to status.ordinal.toString()
            )))
        }
        assertPayments(listOf(other), database.dao.getAll(Columns.COURSE, other.course.toString()))
        assertPayments(emptyList(), database.dao.getAll(Columns.COURSE, "-1"))
    }
}
