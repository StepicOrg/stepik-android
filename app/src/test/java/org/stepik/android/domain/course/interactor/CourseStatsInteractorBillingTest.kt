package org.stepik.android.domain.course.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.*
import org.junit.Test
import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.course_purchase.model.CoursePurchaseFlow
import org.stepik.android.domain.mobile_tiers.interactor.MobileTiersInteractor
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.*
import org.stepik.android.model.user.Profile
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

class CourseStatsInteractorBillingTest {
    private fun paid(id: Long = 42, enabled: Boolean? = true): Course = Course(id, isPaid = true,
        actions = enabled?.let { CourseActions(null, null, CourseBuyAction(it), null) })

    private class Fixture(flow: String = "IAP") {
        data class PaymentRequest(val course: Long, val status: CoursePayment.Status?, val source: DataSourceType)
        data class PriceRequest(val type: String, val ids: List<String>, val source: DataSourceType)
        val config = mock<FirebaseRemoteConfig>()
        val configValue = mock<FirebaseRemoteConfigValue>()
        val profiles = mock<ProfileRepository>()
        val progressRepository = mock<ProgressRepository>()
        val paymentRequests = mutableListOf<PaymentRequest>()
        val tierRequests = mutableListOf<Pair<List<MobileTierCalculation>, DataSourceType>>()
        val priceRequests = mutableListOf<PriceRequest>()
        val reviewRequests = mutableListOf<Pair<List<Long>, DataSourceType>>()
        val progressRequests = mutableListOf<Pair<List<String>, DataSourceType>>()
        val promoRequests = mutableListOf<Pair<Long, String>>()
        var reviewResults = emptyList<CourseReviewSummary>()
        var progressResults = emptyList<Progress>()
        var reviewFailure: Throwable? = null
        var progressFailure: Throwable? = null
        val paymentResults = mutableMapOf<Long, List<CoursePayment>>()
        val paymentFailures = mutableSetOf<Long>()
        val paymentGates = mutableMapOf<Long, SingleSubject<List<CoursePayment>>>()
        var tierResults = listOf(MobileTier("tier", 42, "standard", "promo"))
        val standard = LightSku("standard", "12,34 €")
        val promo = LightSku("promo", "9,99 €")
        var priceResults = listOf(standard, promo)
        var tierFailure = false
        var priceFailure = false
        var promoFailure = false
        var promoResult = DeeplinkPromoCode("ПРОМО", "9.99", "EUR")

        val reviews = object : CourseReviewSummaryRepository {
            override fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long, sourceType: DataSourceType) = Single.defer {
                reviewRequests += courseReviewSummaryIds.toList() to sourceType
                reviewFailure?.let { Single.error(it) } ?: Single.just(reviewResults)
            }
        }
        val payments = object : CoursePaymentsRepository {
            override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?, sourceType: DataSourceType) = Single.defer {
                paymentRequests += PaymentRequest(courseId, coursePaymentStatus, sourceType)
                when {
                    courseId in paymentFailures -> Single.error(IllegalStateException("synthetic payment failure"))
                    courseId in paymentGates -> paymentGates.getValue(courseId)
                    else -> Single.just(paymentResults[courseId].orEmpty())
                }
            }
            override fun checkDeeplinkPromoCodeValidity(courseId: Long, name: String) = Single.defer {
                promoRequests += courseId to name
                if (promoFailure) Single.error(IllegalStateException("synthetic promo failure")) else Single.just(promoResult)
            }
            override fun createCoursePayment(courseId: Long, sku: ProductDetails, purchase: Purchase, promoCode: String?): Single<CoursePayment> =
                Single.error(AssertionError("unexpected payment creation"))
        }
        val tiers = object : MobileTiersRepository {
            override fun calculateMobileTiers(mobileTierCalculations: List<MobileTierCalculation>, dataSourceType: DataSourceType) = Single.defer {
                tierRequests += mobileTierCalculations to dataSourceType
                if (tierFailure) Single.error(IllegalStateException("synthetic tier failure")) else Single.just(tierResults)
            }
        }
        val prices = object : LightSkuRepository {
            override fun getLightInventory(productType: String, skuIds: List<String>, dataSourceType: DataSourceType) = Single.defer {
                priceRequests += PriceRequest(productType, skuIds, dataSourceType)
                if (priceFailure) Single.error(IllegalStateException("synthetic price failure")) else Single.just(priceResults.filter { it.id in skuIds })
            }
        }
        val interactor = CourseStatsInteractor(reviews, payments, progressRepository, profiles, tiers, prices,
            MobileTiersInteractor(tiers, prices), config)

        init {
            whenever(config.getValue(RemoteConfig.PURCHASE_FLOW_ANDROID)).thenReturn(configValue)
            whenever(configValue.asString()).thenReturn(flow)
            whenever(profiles.getProfile(DataSourceType.CACHE)).thenReturn(Single.just(Profile(71)))
            whenever(progressRepository.getProgresses(any(), any())).thenAnswer { invocation ->
                val ids = invocation.getArgument<List<String>>(0)
                val source = invocation.getArgument<DataSourceType>(1)
                Single.defer {
                    progressRequests += ids to source
                    progressFailure?.let { Single.error<List<Progress>>(it) } ?: Single.just(progressResults)
                }
            }
        }

        fun states(courses: List<Course>, mobile: Boolean, resolve: Boolean = true): List<EnrollmentState> {
            val observer = if (mobile) interactor.getCourseStatsMobileTiers(courses, resolveEnrollmentState = resolve).test()
                else interactor.getCourseStats(courses, resolveEnrollmentState = resolve).test()
            observer.assertComplete().assertNoErrors().assertValueCount(1)
            return observer.values().single().map { it.enrollmentState }
        }
    }

    @Test fun enrolledCourseUsesCurrentProfileAndPreservesArchiveFavoriteFlagsWithoutPaymentLookup() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            val course = paid().copy(enrollment = 1, isArchived = true, isFavorite = true)
            val state = f.states(listOf(course), mobile).single() as EnrollmentState.Enrolled
            assertEquals(42L, state.userCourse.course)
            assertEquals(71L, state.userCourse.user)
            assertTrue(state.userCourse.isArchived)
            assertTrue(state.userCourse.isFavorite)
            assertNull(state.userCourse.lastViewed)
            assertTrue(f.paymentRequests.isEmpty())
            verify(f.profiles).getProfile(DataSourceType.CACHE)
        }
    }

    @Test fun successfulPaymentMakesPaidUnenrolledCourseEligibleToEnrollWithoutBuyingAgain() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            f.paymentResults[42] = listOf(CoursePayment(101, 42, true, CoursePayment.Status.SUCCESS, 71))
            assertEquals(listOf(EnrollmentState.NotEnrolledFree), f.states(listOf(paid(enabled = false)), mobile))
            assertEquals(listOf(Fixture.PaymentRequest(42, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE)), f.paymentRequests)
        }
    }

    @Test fun paidUnownedCourseUsesBuyEnabledFlagForWebAndMobileStates() {
        for (mobile in listOf(false, true)) for (enabled in listOf(true, false, null)) {
            val f = Fixture()
            val expected = if (enabled != true) EnrollmentState.NotEnrolledCantBeBought
                else if (mobile) EnrollmentState.NotEnrolledMobileTier(f.standard, f.promo) else EnrollmentState.NotEnrolledWeb
            assertEquals(listOf(expected), f.states(listOf(paid(enabled = enabled)), mobile))
        }
    }

    @Test fun freeAndEndedPaidCoursesSkipPaymentLookup() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            assertEquals(listOf(EnrollmentState.NotEnrolledFree, EnrollmentState.NotEnrolledEnded),
                f.states(listOf(Course(42), paid(43).copy(scheduleType = Course.SCHEDULE_TYPE_ENDED)), mobile))
            assertTrue(f.paymentRequests.isEmpty())
        }
    }

    @Test fun mobileTierStateRequiresStandardPriceAndMakesPromoOptional() {
        for (withPromo in listOf(true, false)) {
            val f = Fixture()
            if (!withPromo) f.priceResults = listOf(f.standard)
            assertEquals(listOf(EnrollmentState.NotEnrolledMobileTier(f.standard, if (withPromo) f.promo else null)), f.states(listOf(paid()), true))
        }
    }

    @Test fun missingStandardPriceUsesStrictIapUnavailableOrWebFallbackEvenIfPromoPriceExists() {
        for (flow in CoursePurchaseFlow.entries) {
            val f = Fixture(flow.name.lowercase())
            f.priceResults = listOf(f.promo)
            assertEquals(listOf(if (flow == CoursePurchaseFlow.IAP) EnrollmentState.NotEnrolledUnavailableIAP else EnrollmentState.NotEnrolledWeb),
                f.states(listOf(paid()), true))
        }
    }

    @Test fun paymentErrorReturnsWebOrAvailableMobileTierFallback() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            f.paymentFailures += 42
            assertEquals(listOf(if (mobile) EnrollmentState.NotEnrolledMobileTier(f.standard, f.promo) else EnrollmentState.NotEnrolledWeb),
                f.states(listOf(paid(enabled = false)), mobile))
        }
    }

    @Test fun paymentErrorWithoutStandardPriceUsesConfiguredFallback() {
        for (flow in CoursePurchaseFlow.entries) {
            val f = Fixture(flow.name)
            f.paymentFailures += 42
            f.priceResults = emptyList()
            assertEquals(listOf(if (flow == CoursePurchaseFlow.IAP) EnrollmentState.NotEnrolledUnavailableIAP else EnrollmentState.NotEnrolledWeb),
                f.states(listOf(paid()), true))
        }
    }

    @Test fun realMobileTiersInteractorConvertsTierAndPriceErrorsToUnavailableOrWebState() {
        for (flow in CoursePurchaseFlow.entries) for (tierFailure in listOf(true, false)) {
            val f = Fixture(flow.name)
            f.tierFailure = tierFailure
            f.priceFailure = !tierFailure
            assertEquals(listOf(if (flow == CoursePurchaseFlow.IAP) EnrollmentState.NotEnrolledUnavailableIAP else EnrollmentState.NotEnrolledWeb),
                f.states(listOf(paid()), true))
            if (tierFailure) assertTrue(f.priceRequests.isEmpty())
        }
    }

    @Test fun disablingEnrollmentResolutionSkipsPaymentsButWebAndMobileKeepDifferentPaidStates() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            assertEquals(listOf(if (mobile) EnrollmentState.NotEnrolledMobileTier(f.standard, f.promo) else EnrollmentState.NotEnrolledFree),
                f.states(listOf(paid(enabled = false)), mobile, resolve = false))
            assertTrue(f.paymentRequests.isEmpty())
        }
    }

    @Test fun mixedCoursesAssociateReviewsProgressPaymentsAndTiersByIdDespiteOutOfOrderPayments() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            val courses = listOf(paid(43).copy(learnersCount = 43, reviewSummary = 143, progress = "progress43", readiness = 0.43),
                Course(99, learnersCount = 99, reviewSummary = 199),
                paid(42).copy(learnersCount = 42, reviewSummary = 142, progress = "progress42", readiness = 0.42))
            f.reviewResults = listOf(CourseReviewSummary(142, 42, 4.2, 1, emptyList()), CourseReviewSummary(143, 43, 4.3, 1, emptyList()))
            f.progressResults = listOf(Progress("progress42", nStepsPassed = 42), Progress("progress43", nStepsPassed = 43))
            f.tierResults = listOf(MobileTier("tier42", 42, "standard"), MobileTier("tier43", 43, "sku43"))
            val price43 = LightSku("sku43", "43,00 €")
            f.priceResults = listOf(price43, f.standard)
            f.paymentGates[42] = SingleSubject.create()
            f.paymentGates[43] = SingleSubject.create()
            val source = SourceTypeComposition(DataSourceType.CACHE, DataSourceType.REMOTE)
            val observer = if (mobile) f.interactor.getCourseStatsMobileTiers(courses, source).test() else f.interactor.getCourseStats(courses, source).test()
            try {
                observer.assertNoValues().assertNotComplete()
                f.paymentGates.getValue(42).onSuccess(listOf(CoursePayment(101, 42, true, CoursePayment.Status.SUCCESS, 71)))
                observer.assertNoValues()
                f.paymentGates.getValue(43).onSuccess(emptyList())
                observer.assertComplete().assertNoErrors().assertValueCount(1)
                val result = observer.values().single()
                assertEquals(listOf(43L, 99L, 42L), result.map { it.learnersCount })
                assertEquals(listOf(4.3, 0.0, 4.2), result.map { it.review })
                assertEquals(listOf(0.43, 0.0, 0.42), result.map { it.readiness })
                assertEquals(listOf(f.progressResults[1], null, f.progressResults[0]), result.map { it.progress })
                assertEquals(listOf(if (mobile) EnrollmentState.NotEnrolledMobileTier(price43, null) else EnrollmentState.NotEnrolledWeb,
                    EnrollmentState.NotEnrolledFree, EnrollmentState.NotEnrolledFree), result.map { it.enrollmentState })
                assertEquals(listOf(listOf(143L, 199L, 142L) to DataSourceType.CACHE), f.reviewRequests)
                assertEquals(listOf(listOf("progress43", "progress42") to DataSourceType.CACHE), f.progressRequests)
                assertEquals(listOf(43L, 42L), f.paymentRequests.map { it.course })
                assertTrue(f.paymentRequests.all { it.source == DataSourceType.REMOTE && it.status == CoursePayment.Status.SUCCESS })
            } finally { observer.dispose() }
        }
    }

    @Test fun sourceCompositionRoutesEnrollmentCacheSeparatelyFromGeneralRemoteData() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            val courses = listOf(paid().copy(reviewSummary = 142, progress = "p42"), Course(77, enrollment = 1, reviewSummary = 177))
            val source = SourceTypeComposition(DataSourceType.REMOTE, DataSourceType.CACHE)
            val observer = if (mobile) f.interactor.getCourseStatsMobileTiers(courses, source).test() else f.interactor.getCourseStats(courses, source).test()
            observer.assertComplete().assertNoErrors()
            assertEquals(listOf(Fixture.PaymentRequest(42, CoursePayment.Status.SUCCESS, DataSourceType.CACHE)), f.paymentRequests)
            assertEquals(listOf(listOf(142L) to DataSourceType.REMOTE), f.reviewRequests)
            assertEquals(listOf(listOf("p42") to DataSourceType.REMOTE), f.progressRequests)
            if (mobile) {
                assertEquals(DataSourceType.CACHE, f.priceRequests.single().source)
                // Documented MobileTiersInteractor limitation: tier calculations keep default REMOTE.
                assertEquals(DataSourceType.REMOTE, f.tierRequests.single().second)
            }
        }
    }

    @Test fun reviewFailureFallsBackToZeroWhileProgressFailurePropagates() {
        for (mobile in listOf(false, true)) {
            val f = Fixture()
            val failure = IllegalStateException("synthetic statistics failure")
            f.reviewFailure = failure
            val first = if (mobile) f.interactor.getCourseStatsMobileTiers(listOf(Course(42))).test() else f.interactor.getCourseStats(listOf(Course(42))).test()
            first.assertComplete().assertValue { it.single().review == 0.0 }
            f.progressFailure = failure
            val second = if (mobile) f.interactor.getCourseStatsMobileTiers(listOf(Course(42))).test() else f.interactor.getCourseStats(listOf(Course(42))).test()
            second.assertNoValues().assertError { it === failure }
        }
    }

    @Test fun knownBoundaryEndedFreeCourseDiffersBetweenWebAndMobileTierPaths() {
        val course = Course(42, scheduleType = Course.SCHEDULE_TYPE_ENDED)
        val web = Fixture()
        val mobile = Fixture()
        assertEquals(listOf(EnrollmentState.NotEnrolledEnded), web.states(listOf(course), false))
        assertEquals(listOf(EnrollmentState.NotEnrolledFree), mobile.states(listOf(course), true))
        assertTrue(web.paymentRequests.isEmpty())
        assertTrue(mobile.paymentRequests.isEmpty())
    }

    @Test fun inAppFlowsRequestRemotePromoTierAndPriceWhileWebSkipsBoth() {
        for (flow in CoursePurchaseFlow.entries) {
            val f = Fixture(flow.name.lowercase())
            val expectedSku = if (flow.isInAppActive()) PromoCodeSku("ПРОМО", f.promo) else PromoCodeSku.EMPTY
            f.interactor.checkDeeplinkPromoCodeValidity(42, "ПРОМО").test().assertResult(f.promoResult to expectedSku)
            assertEquals(listOf(42L to "ПРОМО"), f.promoRequests)
            if (flow.isInAppActive()) {
                assertEquals(listOf(listOf(MobileTierCalculation(42, promo = "ПРОМО")) to DataSourceType.REMOTE), f.tierRequests)
                assertEquals(listOf(Fixture.PriceRequest(BillingClient.ProductType.INAPP, listOf("promo"), DataSourceType.REMOTE)), f.priceRequests)
            } else {
                assertTrue(f.tierRequests.isEmpty())
                assertTrue(f.priceRequests.isEmpty())
            }
        }
    }

    @Test fun absentPromoTierReturnsDeeplinkWithEmptySkuAndAbsentPricePreservesPromoNameWithNullSku() {
        for (hasTier in listOf(false, true)) {
            val f = Fixture()
            if (!hasTier) f.tierResults = listOf(MobileTier("tier", 42, "standard"))
            f.priceResults = emptyList()
            f.interactor.checkDeeplinkPromoCodeValidity(42, "ПРОМО").test()
                .assertResult(f.promoResult to if (hasTier) PromoCodeSku("ПРОМО", null) else PromoCodeSku.EMPTY)
            if (!hasTier) assertTrue(f.priceRequests.isEmpty())
        }
    }

    @Test fun missingPromoCalculationAndEveryPromoSourceFailureReturnEmptyPair() {
        for (stage in listOf("promo", "tier", "price", "missing")) {
            val f = Fixture()
            f.promoFailure = stage == "promo"
            f.tierFailure = stage == "tier"
            f.priceFailure = stage == "price"
            if (stage == "missing") f.tierResults = emptyList()
            f.interactor.checkDeeplinkPromoCodeValidity(42, "ПРОМО").test().assertResult(DeeplinkPromoCode.EMPTY to PromoCodeSku.EMPTY)
            if (stage == "promo") assertTrue(f.tierRequests.isEmpty())
            if (stage != "price") assertTrue(f.priceRequests.isEmpty())
        }
    }

    @Test fun emptyCourseListsProduceEmptyStatsForBothPaths() {
        val f = Fixture()
        assertTrue(f.states(emptyList(), false).isEmpty())
        assertTrue(f.states(emptyList(), true).isEmpty())
        assertTrue(f.paymentRequests.isEmpty())
    }
}
