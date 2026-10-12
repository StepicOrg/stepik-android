package org.stepik.android.cache.base.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.stepic.droid.storage.migration.Migrations
import org.stepic.droid.storage.operations.DatabaseOperationsImpl
import org.stepik.android.cache.course_payments.CoursePaymentsCacheDataSourceImpl
import org.stepik.android.cache.course_payments.dao.CoursePaymentsDaoImpl
import org.stepik.android.cache.course_purchase.BillingPurchasePayloadCacheDataSourceImpl
import org.stepik.android.cache.mobile_tiers.LightSkuCacheDataSourceImpl
import org.stepik.android.cache.mobile_tiers.MobileTiersCacheDataSourceImpl
import org.stepik.android.cache.testutil.BillingStorageFixtures.payment
import org.stepik.android.cache.testutil.BillingStorageFixtures.payload
import org.stepik.android.cache.testutil.assertPayments
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BillingFreshDatabaseTest {
    @Test
    fun productionOnCreateExposesRoomAndLegacyTablesAndAllFourCachePaths() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val name = "billing-fresh-test-${UUID.randomUUID()}.db"
        // Mirror StorageModule's current fresh-install callback; no historical schema fixtures.
        val database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(*Migrations.migrations)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    Migrations.migrations.forEach { it.migrate(db) }
                }
            })
            .build()
        try {
            val sql = database.openHelper.writableDatabase
            val tables = mutableSetOf<String>()
            sql.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
                while (cursor.moveToNext()) tables.add(cursor.getString(0))
            }
            assertTrue(tables.containsAll(setOf("BillingPurchasePayload", "MobileTier", "LightSku", "course_payments")))
            assertEquals(AppDatabase.VERSION, sql.version)

            val payloadCache = BillingPurchasePayloadCacheDataSourceImpl(database.billingPurchasePayloadDao())
            val paymentCache = CoursePaymentsCacheDataSourceImpl(CoursePaymentsDaoImpl(DatabaseOperationsImpl(sql)))
            val tierCache = MobileTiersCacheDataSourceImpl(database.mobileTiersDao())
            val skuCache = LightSkuCacheDataSourceImpl(database.lightSkuDao())
            val purchasePayload = payload()
            val coursePayment = payment()
            val tier = MobileTier("test-tier", coursePayment.course, "test-standard", null)
            val sku = LightSku("test-standard", "5,99 €")
            payloadCache.savePurchaseOrderPayload(purchasePayload).awaitComplete()
            paymentCache.saveCoursePayments(listOf(coursePayment)).awaitComplete()
            tierCache.saveMobileTiers(listOf(tier)).awaitComplete()
            skuCache.saveLightInventory(listOf(sku)).awaitComplete()
            assertEquals(purchasePayload, payloadCache.getPurchaseOrderPayload(purchasePayload.orderId).awaitValue())
            assertPayments(listOf(coursePayment), paymentCache.getCoursePaymentsByCourseId(coursePayment.course, null).awaitValue())
            assertEquals(listOf(tier), tierCache.getMobileTiers(listOf(tier.course)).awaitValue())
            assertEquals(listOf(sku), skuCache.getLightInventory(listOf(sku.id)).awaitValue())
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
