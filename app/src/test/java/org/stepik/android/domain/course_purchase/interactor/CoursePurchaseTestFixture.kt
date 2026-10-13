package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import org.stepik.android.domain.course_purchase.repository.BillingPurchasePayloadRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.interactor.UserCoursesInteractor
import org.stepik.android.model.Course
import org.stepik.android.model.user.Profile
import org.stepik.android.testutil.BillingFixtures

/** Records subscriptions and completions, because production assembles several downstream calls eagerly. */
internal class CoursePurchaseTestFixture {
    enum class Stage { SAVE, VERIFY, CONSUME, DELETE, ADD, CLEAR, REFRESH }
    data class Verification(val course: Long, val sku: ProductDetails, val purchase: Purchase, val promo: String?)

    val billing = mock<BillingRepository>()
    val payments = mock<CoursePaymentsRepository>()
    val tiers = mock<MobileTiersRepository>()
    val prices = mock<LightSkuRepository>()
    val profiles = mock<ProfileRepository>()
    val courses = mock<CourseRepository>()
    val lessons = mock<LessonRepository>()
    val userCourses = mock<UserCoursesInteractor>()
    val updates = PublishSubject.create<Course>()
    val updateObserver = updates.test()
    val sku = BillingFixtures.product("sku-42")
    val refreshed = Course(42, enrollment = 1)
    val events = mutableListOf<String>()
    val gates = mutableMapOf<Stage, CompletableSubject>()
    val failures = mutableMapOf<Stage, Throwable>()
    val payloads = mutableMapOf<String, BillingPurchasePayload>()
    val payloadReads = mutableListOf<String>()
    val verified = mutableListOf<Verification>()
    val consumed = mutableListOf<Purchase>()
    val outstanding = mutableListOf<Purchase>()
    var status = CoursePayment.Status.SUCCESS
    var payloadReadError: Throwable? = null
    var inventoryAvailable = true

    val payloadRepository = object : BillingPurchasePayloadRepository {
        override fun saveBillingPurchasePayload(billingPurchasePayload: BillingPurchasePayload): Completable =
            operation(Stage.SAVE) { payloads[billingPurchasePayload.orderId] = billingPurchasePayload }

        override fun getBillingPurchasePayload(orderId: String): Single<BillingPurchasePayload> = Single.defer {
            payloadReads += orderId
            events += "payload:read"
            val error = payloadReadError
            val value = payloads[orderId]
            when {
                error != null -> Single.error(error)
                value == null -> Single.error(NoSuchElementException("synthetic missing payload"))
                else -> Single.just(value)
            }
        }

        override fun deleteBillingPurchasePayload(orderId: String): Completable =
            operation(Stage.DELETE) { payloads.remove(orderId) }
    }

    val interactor = CoursePurchaseInteractor(userCourses, billing, tiers, prices, payments, courses,
        lessons, profiles, payloadRepository, updates)

    init {
        whenever(profiles.getProfile(DataSourceType.CACHE)).thenReturn(Single.defer {
            events += "profile:read"
            Single.just(Profile(71))
        })
        whenever(billing.getAllPurchases(BillingClient.ProductType.INAPP)).thenReturn(Single.defer {
            events += "purchases:read"
            Single.just(outstanding.toList())
        })
        whenever(billing.getInventory(eq(BillingClient.ProductType.INAPP), any<String>())).thenAnswer {
            Maybe.defer {
                events += "inventory:read"
                if (inventoryAvailable) Maybe.just(sku) else Maybe.empty<ProductDetails>()
            }
        }
        whenever(payments.createCoursePayment(any(), any(), any(), anyOrNull())).thenAnswer { invocation ->
            val request = Verification(invocation.getArgument(0), invocation.getArgument(1),
                invocation.getArgument(2), invocation.getArgument(3))
            Single.defer {
                verified += request
                operation(Stage.VERIFY).andThen(Single.just(CoursePayment(101, request.course, true, status, 71)))
            }
        }
        whenever(billing.consumePurchase(any())).thenAnswer { invocation ->
            val purchase = invocation.getArgument<Purchase>(0)
            operation(Stage.CONSUME) { consumed += purchase; outstanding.remove(purchase) }
        }
        whenever(userCourses.addUserCourse(42)).thenReturn(operation(Stage.ADD))
        whenever(lessons.removeCachedLessons(42)).thenReturn(operation(Stage.CLEAR))
        whenever(courses.getCourse(42, DataSourceType.REMOTE, false)).thenReturn(
            operation(Stage.REFRESH).andThen(Maybe.just(refreshed)))
    }

    private fun operation(stage: Stage, mutation: () -> Unit = {}): Completable = Completable.defer {
        events += "$stage:subscribe"
        val error = failures[stage]
        (if (error != null) Completable.error(error) else gates[stage] ?: Completable.complete())
            .doOnComplete { mutation(); events += "$stage:complete" }
            .doOnError { events += "$stage:error" }
    }

    fun purchase(
        account: String? = "1754", profile: String? = "2243", identifiers: Boolean = true,
        order: String? = "synthetic-order", products: List<String> = listOf("sku-42"),
        state: Int = Purchase.PurchaseState.PURCHASED
    ): Purchase {
        val ids = if (identifiers) mock<AccountIdentifiers> {
            on { obfuscatedAccountId } doReturn account
            on { obfuscatedProfileId } doReturn profile
        } else null
        return BillingFixtures.purchase(products, state).also {
            whenever(it.accountIdentifiers).thenReturn(ids)
            whenever(it.orderId).thenReturn(order)
        }
    }

    fun storedPayload(purchase: Purchase, promo: String? = "ПРОМО"): BillingPurchasePayload =
        BillingPurchasePayload(purchase.orderId.orEmpty(), 42, 71,
            purchase.accountIdentifiers?.obfuscatedAccountId.orEmpty(),
            purchase.accountIdentifiers?.obfuscatedProfileId.orEmpty(), promo)

    fun consume(purchase: Purchase, promo: String? = "ПРОМО"): Completable =
        interactor.consumePurchase(42, 71, sku, purchase, promo)

    fun subscribedStages(): List<Stage> = Stage.entries.filter { "$it:subscribe" in events }

    fun close() { updateObserver.dispose() }
}
