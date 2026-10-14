package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.domain.course.model.CoursePurchasePayload
import org.stepik.android.domain.course_payments.exception.CourseAlreadyOwnedException
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseTestFixture.Stage
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.model.user.Profile
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

class CoursePurchaseInteractorTest {
    private val fixtures = mutableListOf<CoursePurchaseTestFixture>()
    private fun fixture() = CoursePurchaseTestFixture().also(fixtures::add)
    private val failure = IllegalStateException("synthetic failure")
    @After fun closeObservers() { fixtures.forEach { it.close() } }

    @Test fun ownedCourseRejectsPurchaseWithoutProfileOrInventorySubscription() {
        val f = fixture()
        whenever(f.payments.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE))
            .thenReturn(Single.just(listOf(CoursePayment(101, 42, true, CoursePayment.Status.SUCCESS, 71))))
        f.interactor.fetchPurchaseFlowData(42, "sku-42").test().assertNoValues().assertError(CourseAlreadyOwnedException::class.java)
        assertTrue(f.events.isEmpty())
        verifyNoInteractions(f.profiles, f.billing)
    }

    @Test fun preflightWaitsForPaymentsThenProfileAndMapsDeterministicRestoreIdentifiers() {
        val f = fixture()
        val payments = SingleSubject.create<List<CoursePayment>>()
        val profile = SingleSubject.create<Profile>()
        whenever(f.payments.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE)).thenReturn(payments)
        whenever(f.profiles.getProfile(DataSourceType.CACHE)).thenReturn(profile)
        val observer = f.interactor.fetchPurchaseFlowData(42, "sku-42").test()
        try {
            observer.assertNoValues().assertNotComplete()
            assertFalse(profile.hasObservers())
            verifyNoInteractions(f.billing)
            payments.onSuccess(emptyList())
            assertTrue(profile.hasObservers())
            verifyNoInteractions(f.billing)
            profile.onSuccess(Profile(71))
            observer.assertComplete().assertValue { value ->
                value.coursePurchasePayload == CoursePurchasePayload(71, 42) &&
                    value.obfuscatedParams.obfuscatedAccountId == "1754" &&
                    value.obfuscatedParams.obfuscatedProfileId == "2243" && value.skuDetails === f.sku
            }
            verify(f.billing).getInventory(BillingClient.ProductType.INAPP, "sku-42")
        } finally { observer.dispose() }
    }

    @Test fun preflightPaymentProfileAndInventoryFailuresStopLaterSubscriptions() {
        for (stage in listOf("payments", "profile", "inventory", "missing")) {
            val f = fixture()
            whenever(f.payments.getCoursePaymentsByCourseId(42, CoursePayment.Status.SUCCESS, DataSourceType.REMOTE))
                .thenReturn(if (stage == "payments") Single.error(failure) else Single.just(emptyList()))
            if (stage == "profile") whenever(f.profiles.getProfile(DataSourceType.CACHE)).thenReturn(Single.error(failure))
            if (stage == "inventory") whenever(f.billing.getInventory(BillingClient.ProductType.INAPP, "sku-42")).thenReturn(Maybe.error(failure))
            f.inventoryAvailable = stage != "missing"
            val observer = f.interactor.fetchPurchaseFlowData(42, "sku-42").test()
            if (stage == "missing") observer.assertError(NoSuchElementException::class.java) else observer.assertError { it === failure }
            observer.assertNoValues()
            if (stage in listOf("payments", "profile")) verifyNoInteractions(f.billing)
            assertTrue(f.subscribedStages().isEmpty())
        }
    }

    @Test fun promoWithoutTierReturnsEmptyAndSkipsPriceLookup() {
        val f = fixture()
        val calculation = MobileTierCalculation(42, promo = "ПРОМО")
        whenever(f.tiers.calculateMobileTier(calculation, DataSourceType.REMOTE)).thenReturn(Maybe.just(MobileTier("tier", 42, "sku-42")))
        f.interactor.checkPromoCodeValidity(42, "ПРОМО").test().assertResult(PromoCodeSku.EMPTY)
        verifyNoInteractions(f.prices)
    }

    @Test fun promoTierRequestsRemoteInAppPriceAndAllowsMissingSku() {
        for (prices in listOf(listOf(LightSku("promo-sku", "9,99 €")), emptyList())) {
            val f = fixture()
            whenever(f.tiers.calculateMobileTier(MobileTierCalculation(42, promo = "ПРОМО"), DataSourceType.REMOTE))
                .thenReturn(Maybe.just(MobileTier("tier", 42, "sku-42", "promo-sku")))
            whenever(f.prices.getLightInventory(BillingClient.ProductType.INAPP, listOf("promo-sku"), DataSourceType.REMOTE)).thenReturn(Single.just(prices))
            f.interactor.checkPromoCodeValidity(42, "ПРОМО").test().assertResult(PromoCodeSku("ПРОМО", prices.firstOrNull()))
        }
    }

    @Test fun missingPromoTierAndTierOrPriceErrorsPropagate() {
        for (stage in listOf("missing", "tier", "price")) {
            val f = fixture()
            whenever(f.tiers.calculateMobileTier(MobileTierCalculation(42, promo = "promo"), DataSourceType.REMOTE))
                .thenReturn(when (stage) {
                    "missing" -> Maybe.empty()
                    "tier" -> Maybe.error(failure)
                    else -> Maybe.just(MobileTier("tier", 42, "sku-42", "promo-sku"))
                })
            whenever(f.prices.getLightInventory(BillingClient.ProductType.INAPP, listOf("promo-sku"), DataSourceType.REMOTE)).thenReturn(Single.error(failure))
            val observer = f.interactor.checkPromoCodeValidity(42, "promo").test().assertNoValues()
            if (stage == "missing") observer.assertError(NoSuchElementException::class.java) else observer.assertError { it === failure }
            if (stage != "price") verifyNoInteractions(f.prices)
        }
    }

    @Test fun payloadMapsAllFieldsIncludingNullablePromoAndAbsentIdentifiersLazily() {
        for (identifiers in listOf(true, false)) for (promo in listOf(null, "", "ПРОМО")) {
            val f = fixture()
            val purchase = f.purchase(identifiers = identifiers)
            val stream = f.interactor.saveBillingPurchasePayload(42, 71, purchase, promo)
            assertTrue(f.payloads.isEmpty())
            stream.test().assertResult()
            assertEquals(BillingPurchasePayload("synthetic-order", 42, 71,
                if (identifiers) "1754" else "", if (identifiers) "2243" else "", promo), f.payloads["synthetic-order"])
            assertEquals(listOf(Stage.SAVE), f.subscribedStages())
        }
    }

    @Test fun nullableAccountIdentifierFieldsMapToEmptyStrings() {
        val f = fixture()
        f.interactor.saveBillingPurchasePayload(42, 71, f.purchase(account = null, profile = null), null).test().assertResult()
        assertEquals(BillingPurchasePayload("synthetic-order", 42, 71, "", "", null), f.payloads["synthetic-order"])
    }

    @Test fun everyPurchaseStageWaitsForPreviousCompletionAndEmitsExactlyOneEnrollmentUpdate() {
        val f = fixture()
        val purchase = f.purchase()
        Stage.entries.forEach { f.gates[it] = CompletableSubject.create() }
        val stream = f.consume(purchase)
        assertTrue(f.events.isEmpty())
        val observer = stream.test()
        try {
            Stage.entries.forEachIndexed { index, stage ->
                observer.assertNotComplete().assertNoErrors()
                f.updateObserver.assertNoValues()
                assertEquals(Stage.entries.take(index + 1), f.subscribedStages())
                assertEquals(Stage.entries.take(index).map { "$it:complete" }, f.events.filter { it.endsWith(":complete") })
                assertTrue(f.gates.getValue(stage).hasObservers())
                f.gates.getValue(stage).onComplete()
                if (stage == Stage.SAVE) assertEquals(f.storedPayload(purchase), f.payloads[purchase.orderId])
                if (stage == Stage.DELETE) assertTrue(f.payloads.isEmpty())
            }
            observer.assertResult()
            f.updateObserver.assertValue(f.refreshed).assertNoErrors()
            assertEquals(listOf(CoursePurchaseTestFixture.Verification(42, f.sku, purchase, "ПРОМО")), f.verified)
            assertEquals(listOf(purchase), f.consumed)
            verify(f.userCourses).addUserCourse(42)
            verify(f.lessons).removeCachedLessons(42)
            verify(f.courses).getCourse(42, DataSourceType.REMOTE, false)
        } finally { observer.dispose() }
    }

    @Test fun failuresAtEveryPurchaseStageStopLaterSubscriptionsAndPreserveExpectedRecoveryState() {
        for (stage in Stage.entries) {
            val f = fixture()
            val purchase = f.purchase()
            f.failures[stage] = failure
            f.consume(purchase).test().assertNotComplete().assertError { it === failure }
            assertEquals(Stage.entries.take(stage.ordinal + 1), f.subscribedStages())
            assertEquals(Stage.entries.take(stage.ordinal).map { "$it:complete" }, f.events.filter { it.endsWith(":complete") })
            assertEquals(stage.ordinal in Stage.VERIFY.ordinal..Stage.DELETE.ordinal, f.payloads.containsKey(purchase.orderId))
            assertEquals(if (stage.ordinal > Stage.CONSUME.ordinal) listOf(purchase) else emptyList(), f.consumed)
            f.updateObserver.assertNoValues()
        }
    }

    @Test fun missingCourseAfterConsumptionFailsWithoutSuccessEvent() {
        val f = fixture()
        whenever(f.courses.getCourse(42, DataSourceType.REMOTE, false)).thenReturn(Maybe.empty())
        f.consume(f.purchase()).test().assertError(NoSuchElementException::class.java).assertNotComplete()
        assertEquals(1, f.consumed.size)
        assertTrue(f.payloads.isEmpty())
        f.updateObserver.assertNoValues()
    }

    @Test fun restoreRequiresBothAccountAndCourseHashesAndRejectsAbsentIdentifiers() {
        val f = fixture()
        val matching = f.purchase()
        val invalid = listOf(f.purchase(account = "wrong"), f.purchase(profile = "wrong"), f.purchase(identifiers = false),
            f.purchase(account = null), f.purchase(profile = null))
        for (purchases in listOf(emptyList<Purchase>()) + invalid.map { listOf(it) }) {
            f.outstanding.clear()
            f.outstanding.addAll(purchases)
            f.interactor.restorePurchase(42).test().assertError(NoPurchasesToRestoreException::class.java).assertNotComplete()
            assertTrue(f.verified.isEmpty())
            assertTrue(f.consumed.isEmpty())
            assertTrue(f.payloadReads.isEmpty())
        }
        f.outstanding.addAll(invalid + matching)
        f.interactor.restorePurchase(42).test().assertResult()
        assertEquals(listOf(matching), f.consumed)
        assertEquals(listOf(matching), f.verified.map { it.purchase })
    }

    @Test fun restoreUsesCurrentProfileRatherThanAcceptingAnotherAccountsPurchase() {
        val f = fixture()
        whenever(f.profiles.getProfile(DataSourceType.CACHE)).thenReturn(Single.just(Profile(72)))
        f.outstanding += f.purchase()
        f.interactor.restorePurchase(42).test().assertError(NoPurchasesToRestoreException::class.java)
        assertTrue(f.subscribedStages().isEmpty())
        verifyNoInteractions(f.payments)
    }

    @Test fun restoreProfileAndPurchaseQueryErrorsPreventCompletionSideEffects() {
        for (profileError in listOf(true, false)) {
            val f = fixture()
            if (profileError) whenever(f.profiles.getProfile(DataSourceType.CACHE)).thenReturn(Single.error(failure))
            else whenever(f.billing.getAllPurchases(BillingClient.ProductType.INAPP)).thenReturn(Single.error(failure))
            f.interactor.restorePurchase(42).test().assertError { it === failure }.assertNotComplete()
            assertTrue(f.verified.isEmpty())
            assertTrue(f.payloadReads.isEmpty())
        }
    }

    @Test fun restoreReusesPromoOnlyWhenBothStoredIdentifiersMatch() {
        for (mismatch in listOf("none", "account", "profile")) {
            val f = fixture()
            val purchase = f.purchase()
            f.outstanding += purchase
            val payload = f.storedPayload(purchase)
            f.payloads[payload.orderId] = when (mismatch) {
                "account" -> payload.copy(obfuscatedAccountId = "wrong")
                "profile" -> payload.copy(obfuscatedProfileId = "wrong")
                else -> payload
            }
            f.interactor.restorePurchase(42).test().assertResult()
            assertEquals(if (mismatch == "none") "ПРОМО" else null, f.verified.single().promo)
            assertEquals(listOf(purchase.orderId), f.payloadReads)
            verify(f.billing).getInventory(BillingClient.ProductType.INAPP, "sku-42")
            assertEquals(Stage.entries.drop(1), f.subscribedStages())
            f.updateObserver.assertValue(f.refreshed)
        }
    }

    @Test fun missingOrUnreadableRestorePayloadFallsBackWithoutPromo() {
        for (readError in listOf(false, true)) {
            val f = fixture()
            val purchase = f.purchase()
            f.outstanding += purchase
            if (readError) f.payloadReadError = failure
            f.interactor.restorePurchase(42).test().assertResult()
            assertNull(f.verified.single().promo)
            assertEquals(listOf(purchase), f.consumed)
        }
    }

    @Test fun restoreUnavailableProductAndInventoryErrorsPreventVerificationAndConsumption() {
        for (missing in listOf(true, false)) {
            val f = fixture()
            val purchase = f.purchase()
            f.outstanding += purchase
            f.payloads[purchase.orderId!!] = f.storedPayload(purchase)
            f.inventoryAvailable = !missing
            if (!missing) whenever(f.billing.getInventory(BillingClient.ProductType.INAPP, "sku-42")).thenReturn(Maybe.error(failure))
            val observer = f.interactor.restorePurchase(42).test().assertNotComplete()
            if (missing) observer.assertError(NoSuchElementException::class.java) else observer.assertError { it === failure }
            assertTrue(f.verified.isEmpty())
            assertTrue(f.consumed.isEmpty())
            assertEquals(f.storedPayload(purchase), f.payloads[purchase.orderId])
            f.updateObserver.assertNoValues()
        }
    }

    @Test fun restoreWaitsAtEveryCompletionStageWithoutResavingPayload() {
        val f = fixture()
        val purchase = f.purchase()
        f.outstanding += purchase
        f.payloads[purchase.orderId!!] = f.storedPayload(purchase)
        val stages = Stage.entries.drop(1)
        stages.forEach { f.gates[it] = CompletableSubject.create() }
        val observer = f.interactor.restorePurchase(42).test()
        try {
            stages.forEachIndexed { index, stage ->
                observer.assertNotComplete().assertNoErrors()
                assertEquals(stages.take(index + 1), f.subscribedStages())
                f.updateObserver.assertNoValues()
                assertTrue(f.gates.getValue(stage).hasObservers())
                f.gates.getValue(stage).onComplete()
            }
            observer.assertResult()
            f.updateObserver.assertValue(f.refreshed)
        } finally { observer.dispose() }
    }

    @Test fun restoreCompletionFailuresStopDownstreamStages() {
        for (stage in Stage.entries.drop(1)) {
            val f = fixture()
            val purchase = f.purchase()
            f.outstanding += purchase
            f.payloads[purchase.orderId!!] = f.storedPayload(purchase)
            f.failures[stage] = failure
            f.interactor.restorePurchase(42).test().assertNotComplete().assertError { it === failure }
            assertEquals(Stage.entries.drop(1).take(stage.ordinal), f.subscribedStages())
            assertEquals(stage.ordinal <= Stage.DELETE.ordinal, f.payloads.containsKey(purchase.orderId))
            f.updateObserver.assertNoValues()
        }
    }
}
