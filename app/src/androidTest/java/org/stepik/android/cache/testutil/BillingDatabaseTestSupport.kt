package org.stepik.android.cache.testutil

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnitRunner
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.stepic.droid.storage.operations.DatabaseOperationsImpl
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.course_payments.dao.CoursePaymentsDaoImpl
import org.stepik.android.cache.course_payments.structure.DbStructureCoursePayments
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import java.util.concurrent.TimeUnit

/** Select together with the debug manifest overlay using -PbillingDatabaseTests=true. */
class BillingDatabaseTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, Application::class.java.name, context)
}

/** Bounded background subscriptions; observers are disposed even after assertion failure. */
fun <T> Single<T>.awaitValue(): T {
    val observer = subscribeOn(Schedulers.io()).test()
    try {
        observer.awaitDone(10, TimeUnit.SECONDS).assertComplete().assertNoErrors().assertValueCount(1)
        return observer.values().single()
    } finally {
        observer.dispose()
    }
}

fun Completable.awaitComplete() {
    val observer = subscribeOn(Schedulers.io()).test()
    try {
        observer.awaitDone(10, TimeUnit.SECONDS).assertComplete().assertNoErrors()
    } finally {
        observer.dispose()
    }
}

fun <T> Single<T>.awaitError(errorClass: Class<out Throwable>) {
    val observer = subscribeOn(Schedulers.io()).test()
    try {
        observer.awaitDone(10, TimeUnit.SECONDS).assertError(errorClass).assertNoValues().assertNotComplete()
    } finally {
        observer.dispose()
    }
}

fun Completable.awaitError(errorClass: Class<out Throwable>) {
    val observer = subscribeOn(Schedulers.io()).test()
    try {
        observer.awaitDone(10, TimeUnit.SECONDS).assertError(errorClass).assertNotComplete()
    } finally {
        observer.dispose()
    }
}

abstract class InMemoryBillingDatabaseTest {
    protected lateinit var database: AppDatabase

    @Before
    fun openDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }
}

/** Real legacy SQL fixture. A trigger can inject failures without replacing the DAO. */
class LegacyPaymentsDatabase : AutoCloseable {
    private val helper: SupportSQLiteOpenHelper = FrameworkSQLiteOpenHelperFactory().create(
        SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(DbStructureCoursePayments.TABLE_SCHEMA)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
    )
    val sql: SupportSQLiteDatabase = helper.writableDatabase
    val dao = CoursePaymentsDaoImpl(DatabaseOperationsImpl(sql))

    override fun close() = helper.close()
}

object BillingStorageFixtures {
    fun payload(orderId: String = "test-order-1", promoCode: String? = "ТЕСТ-优惠") =
        BillingPurchasePayload(orderId, 4_000_000_001L, 5_000_000_002L, "test-account", "test-profile", promoCode)

    fun payment(
        id: Long = 3_000_000_001L,
        course: Long = 4_000_000_001L,
        isPaid: Boolean = true,
        status: CoursePayment.Status = CoursePayment.Status.SUCCESS,
        user: Long = 5_000_000_001L
    ) = CoursePayment(id, course, isPaid, status, user)
}

/** CoursePayment has reference equality, so compare every persisted field by payment ID. */
fun assertPayments(expected: List<CoursePayment>, actual: List<CoursePayment>) {
    fun fields(payment: CoursePayment): List<Any> =
        listOf(payment.id, payment.course, payment.isPaid, payment.status, payment.user)
    assertEquals(expected.size, actual.size)
    assertEquals(expected.associate { it.id to fields(it) }, actual.associate { it.id to fields(it) })
}
