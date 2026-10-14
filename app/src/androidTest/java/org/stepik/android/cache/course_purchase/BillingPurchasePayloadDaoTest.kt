package org.stepik.android.cache.course_purchase

import androidx.room.EmptyResultSetException
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.testutil.BillingStorageFixtures.payload
import org.stepik.android.cache.testutil.InMemoryBillingDatabaseTest
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitError
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

@RunWith(AndroidJUnit4::class)
class BillingPurchasePayloadDaoTest : InMemoryBillingDatabaseTest() {
    @Test
    fun roundTripsEveryFieldAndNullablePromo() {
        val dao = database.billingPurchasePayloadDao()
        val items = listOf(payload(), payload("test-order-2", null), payload("test-order-3", ""))
        items.forEach { dao.saveBillingPurchasePayload(it).awaitComplete() }
        items.forEach { assertEquals(it, dao.getBillingPurchasePayload(it.orderId).awaitValue()) }
    }

    @Test
    fun roundTripsEmptyStringFields() {
        // One empty key is serializable; collisions between purchases remain a deferred defect.
        val item = BillingPurchasePayload("", 0L, 0L, "", "", null)
        val dao = database.billingPurchasePayloadDao()
        dao.saveBillingPurchasePayload(item).awaitComplete()
        assertEquals(item, dao.getBillingPurchasePayload("").awaitValue())
    }

    @Test
    fun sameOrderReplacesEveryFieldAndLeavesOtherOrders() {
        val dao = database.billingPurchasePayloadDao()
        val first = payload()
        val other = payload("test-other-order")
        val replacement = first.copy(
            courseId = 77L, profileId = 88L, obfuscatedAccountId = "", obfuscatedProfileId = "changed", promoCode = null
        )
        listOf(first, other, replacement).forEach { dao.saveBillingPurchasePayload(it).awaitComplete() }
        assertEquals(replacement, dao.getBillingPurchasePayload(first.orderId).awaitValue())
        assertEquals(other, dao.getBillingPurchasePayload(other.orderId).awaitValue())
    }

    @Test
    fun missingOrderReportsRoomEmptyResultError() {
        database.billingPurchasePayloadDao().getBillingPurchasePayload("test-missing-order")
            .awaitError(EmptyResultSetException::class.java)
    }

    @Test
    fun deletionRemovesOnlyRequestedOrderAndAbsentDeletionCompletes() {
        val dao = database.billingPurchasePayloadDao()
        val first = payload()
        val other = payload("test-other-order")
        listOf(first, other).forEach { dao.saveBillingPurchasePayload(it).awaitComplete() }
        dao.deleteBillingPurchasePayload(first.orderId).awaitComplete()
        dao.getBillingPurchasePayload(first.orderId).awaitError(EmptyResultSetException::class.java)
        assertEquals(other, dao.getBillingPurchasePayload(other.orderId).awaitValue())
        dao.deleteBillingPurchasePayload("test-missing-order").awaitComplete()
        assertEquals(other, dao.getBillingPurchasePayload(other.orderId).awaitValue())
    }
}
