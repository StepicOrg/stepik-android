package org.stepik.android.cache.course_purchase

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.testutil.PaymentFixtures
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BillingDatabaseSmokeTest {
    @Test
    fun generatedPayloadDaoWritesAndReadsWithoutProductionApplication() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        assertEquals(Application::class.java, application.javaClass)
        val database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java).build()
        try {
            val dao = database.billingPurchasePayloadDao()
            val payload = PaymentFixtures.payload()
            val save = dao.saveBillingPurchasePayload(payload).subscribeOn(Schedulers.io()).test()
            try {
                save.awaitDone(10, TimeUnit.SECONDS).assertComplete().assertNoErrors()
            } finally {
                save.dispose()
            }
            val read = dao.getBillingPurchasePayload(payload.orderId).subscribeOn(Schedulers.io()).test()
            try {
                read.awaitDone(10, TimeUnit.SECONDS).assertResult(payload)
            } finally {
                read.dispose()
            }
        } finally {
            database.close()
        }
    }
}
