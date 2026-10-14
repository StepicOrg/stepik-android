package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Maybe
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.stepik.android.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.domain.course_purchase.interactor.CoursePurchaseTestFixture.Stage

/** Characterizes open defects listed in the plan; these passing cases do not certify the desired behavior. */
class CoursePurchaseKnownDefectsTest {
    private val fixtures = mutableListOf<CoursePurchaseTestFixture>()
    private fun fixture() = CoursePurchaseTestFixture().also(fixtures::add)
    @After fun closeObservers() { fixtures.forEach { it.close() } }

    @Test fun knownDefectPendingAndUnspecifiedPurchasesReachVerificationAndConsumption() {
        for (state in listOf(Purchase.PurchaseState.PENDING, Purchase.PurchaseState.UNSPECIFIED_STATE)) {
            val f = fixture()
            val purchase = f.purchase(state = state)
            f.consume(purchase).test().assertResult()
            assertEquals(listOf(purchase), f.verified.map { it.purchase })
            assertEquals(listOf(purchase), f.consumed)
            f.updateObserver.assertValue(f.refreshed)
        }
    }

    @Test fun knownDefectRestoreAlsoConsumesPendingAndUnspecifiedPurchasesWithMatchingIdentifiers() {
        for (state in listOf(Purchase.PurchaseState.PENDING, Purchase.PurchaseState.UNSPECIFIED_STATE)) {
            val f = fixture()
            val purchase = f.purchase(state = state)
            f.outstanding += purchase
            f.interactor.restorePurchase(42).test().assertResult()
            assertEquals(listOf(purchase), f.verified.map { it.purchase })
            assertEquals(listOf(purchase), f.consumed)
        }
    }

    @Test fun knownDefectTwoNullOrderIdsUseTheSamePayloadKey() {
        val f = fixture()
        val first = f.purchase(order = null)
        val second = f.purchase(order = null)
        whenever(second.purchaseToken).thenReturn("second-synthetic-token")
        f.interactor.saveBillingPurchasePayload(42, 71, first, "FIRST").test().assertResult()
        f.interactor.saveBillingPurchasePayload(43, 71, second, "SECOND").test().assertResult()
        assertEquals(setOf(""), f.payloads.keys)
        assertEquals(43L, f.payloads.getValue("").courseId)
        assertEquals("SECOND", f.payloads.getValue("").promoCode)
    }

    @Test fun knownDefectNullOrderRestoreReadsAndDeletesEmptyKey() {
        val f = fixture()
        val purchase = f.purchase(order = null)
        f.outstanding += purchase
        f.payloads[""] = f.storedPayload(purchase)
        f.interactor.restorePurchase(42).test().assertResult()
        assertEquals(listOf(""), f.payloadReads)
        assertTrue(f.payloads.isEmpty())
        assertEquals("ПРОМО", f.verified.single().promo)
    }

    @Test fun knownDefectEmptyProductListFailsRestoreBeforeVerification() {
        val f = fixture()
        f.outstanding += f.purchase(products = emptyList())
        f.interactor.restorePurchase(42).test().assertNotComplete().assertError(NoSuchElementException::class.java)
        assertTrue(f.verified.isEmpty())
        assertTrue(f.consumed.isEmpty())
        f.updateObserver.assertNoValues()
        verify(f.billing, never()).getInventory(eq(BillingClient.ProductType.INAPP), any<String>())
    }

    @Test fun knownDefectMultiProductRestoreOnlyQueriesFirstProductEvenWhenSecondaryIsAvailable() {
        val f = fixture()
        f.outstanding += f.purchase(products = listOf("unavailable", "sku-42"))
        whenever(f.billing.getInventory(BillingClient.ProductType.INAPP, "unavailable")).thenReturn(Maybe.empty())
        f.interactor.restorePurchase(42).test().assertNotComplete().assertError(NoSuchElementException::class.java)
        verify(f.billing).getInventory(BillingClient.ProductType.INAPP, "unavailable")
        verify(f.billing, never()).getInventory(BillingClient.ProductType.INAPP, "sku-42")
        assertTrue(f.verified.isEmpty())
        assertTrue(f.consumed.isEmpty())
    }

    @Test fun multipleMatchingOutstandingPurchasesRestoreOnlyTheFirst() {
        val f = fixture()
        val first = f.purchase(order = "first-order")
        val second = f.purchase(order = "second-order")
        f.outstanding += listOf(first, second)
        f.interactor.restorePurchase(42).test().assertResult()
        assertEquals(listOf(first), f.consumed)
        assertEquals(listOf(second), f.outstanding)
        assertEquals(listOf("first-order"), f.payloadReads)
        f.updateObserver.assertValue(f.refreshed)
    }

    @Test fun knownDefectRestoreCannotRetryEnrollmentAfterSuccessfulConsumeAndFailedRefresh() {
        val f = fixture()
        val purchase = f.purchase()
        f.outstanding += purchase
        val failure = IllegalStateException("synthetic refresh failure")
        f.failures[Stage.REFRESH] = failure
        f.consume(purchase).test().assertNotComplete().assertError { it === failure }
        assertTrue(f.payloads.isEmpty())
        assertTrue(f.outstanding.isEmpty())
        assertEquals(listOf(purchase), f.consumed)
        f.failures.clear()
        f.interactor.restorePurchase(42).test().assertNotComplete().assertError(NoPurchasesToRestoreException::class.java)
        assertEquals(1, f.verified.size)
        assertEquals(1, f.consumed.size)
        f.updateObserver.assertNoValues()
    }

    @Test fun knownDefectRepeatingConsumeAfterFailedRefreshRepeatsVerificationAndConsumption() {
        val f = fixture()
        val purchase = f.purchase()
        val failure = IllegalStateException("synthetic refresh failure")
        f.failures[Stage.REFRESH] = failure
        f.consume(purchase).test().assertError { it === failure }
        f.failures.clear()
        f.consume(purchase).test().assertResult()
        assertEquals(listOf(purchase, purchase), f.verified.map { it.purchase })
        assertEquals(listOf(purchase, purchase), f.consumed)
        f.updateObserver.assertValue(f.refreshed)
    }
}
