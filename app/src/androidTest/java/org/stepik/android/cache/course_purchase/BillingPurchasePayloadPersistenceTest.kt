package org.stepik.android.cache.course_purchase

import android.content.Context
import androidx.room.EmptyResultSetException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.testutil.BillingStorageFixtures.payload
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitError
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.data.course_purchase.repository.BillingPurchasePayloadRepositoryImpl
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BillingPurchasePayloadPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "billing-payload-test-${UUID.randomUUID()}.db"
    private var database: AppDatabase? = null

    private fun openRepository(): BillingPurchasePayloadRepositoryImpl {
        val opened = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        database = opened
        return BillingPurchasePayloadRepositoryImpl(BillingPurchasePayloadCacheDataSourceImpl(opened.billingPurchasePayloadDao()))
    }

    private fun closeDatabase() {
        database?.close()
        database = null
    }

    @Before
    fun cleanFile() {
        context.deleteDatabase(name)
    }

    @After
    fun cleanUp() {
        closeDatabase()
        context.deleteDatabase(name)
    }

    @Test
    fun payloadAndDeletionSurviveReopeningWithNewCacheAndRepository() {
        val item = payload()
        val other = payload("test-other-order", null)
        val initial = openRepository()
        initial.saveBillingPurchasePayload(item).awaitComplete()
        initial.saveBillingPurchasePayload(other).awaitComplete()
        assertEquals(item, initial.getBillingPurchasePayload(item.orderId).awaitValue())
        closeDatabase()

        val reopened = openRepository()
        assertEquals(item, reopened.getBillingPurchasePayload(item.orderId).awaitValue())
        assertEquals(other, reopened.getBillingPurchasePayload(other.orderId).awaitValue())
        reopened.deleteBillingPurchasePayload(item.orderId).awaitComplete()
        closeDatabase()

        val reopenedAfterDeletion = openRepository()
        reopenedAfterDeletion.getBillingPurchasePayload(item.orderId).awaitError(EmptyResultSetException::class.java)
        assertEquals(other, reopenedAfterDeletion.getBillingPurchasePayload(other.orderId).awaitValue())
    }
}
