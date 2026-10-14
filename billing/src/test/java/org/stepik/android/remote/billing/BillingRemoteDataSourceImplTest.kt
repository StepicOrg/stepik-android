package org.stepik.android.remote.billing

import com.android.billingclient.api.*
import com.nhaarman.mockitokotlin2.*
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.stepik.android.domain.billing.exception.BillingNotSupportedException
import org.stepik.android.testutil.BillingFixtures
import org.stepik.android.testutil.RxErrorsRule

class BillingRemoteDataSourceImplTest {
    @get:Rule val rxErrors = RxErrorsRule()
    private val client = mock<BillingClient>()
    private val scheduler = TestScheduler()
    private val source = BillingRemoteDataSourceImpl(client, scheduler)

    @Test fun emptyInventorySkipsSdkAndDeliversOnScheduler() {
        val observer = source.getInventory(BillingClient.ProductType.INAPP, emptyList()).test()
        observer.assertNoValues().assertNotComplete()
        org.mockito.Mockito.verifyNoInteractions(client)
        scheduler.triggerActions()
        observer.assertResult(emptyList())
    }

    @Test fun inventoryBuildsRequestedIdsAndTypesAndPreservesFullResponseWithoutOffers() {
        for (type in listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS)) {
            val ids = listOf("course.a", "course.b")
            val observer = source.getInventory(type, ids).test()
            val params = argumentCaptor<QueryProductDetailsParams>()
            val callback = argumentCaptor<ProductDetailsResponseListener>()
            verify(client).queryProductDetailsAsync(params.capture(), callback.capture())
            // Billing 8.0.0 exposes only these obfuscated accessors for query parameters.
            val products = params.firstValue.zza().map { it as QueryProductDetailsParams.Product }
            assertEquals(ids, products.map { it.zza() })
            assertEquals(listOf(type, type), products.map { it.zzb() })
            val details = ids.map { BillingFixtures.product(it, hasOffer = false) }
            callback.firstValue.onProductDetailsResponse(BillingFixtures.result(), QueryProductDetailsResult.create(details, emptyList()))
            observer.assertNoValues().assertNotComplete()
            scheduler.triggerActions()
            observer.assertResult(details)
            clearInvocations(client)
        }
    }

    @Test fun inventoryReturnsOnlyFetchedSubsetWhenOkIncludesUnfetchedProducts() {
        val observer = source.getInventory(BillingClient.ProductType.INAPP, listOf("course.a", "course.b")).test()
        val callback = argumentCaptor<ProductDetailsResponseListener>()
        verify(client).queryProductDetailsAsync(any(), callback.capture())
        val details = listOf(BillingFixtures.product("course.a"))
        val unfetched = mock<UnfetchedProduct> { on { productId } doReturn "course.b" }
        callback.firstValue.onProductDetailsResponse(BillingFixtures.result(), QueryProductDetailsResult.create(details, listOf(unfetched)))
        scheduler.triggerActions()
        observer.assertResult(details)
    }

    @Test fun inventoryEmptyResponseSucceeds() {
        val observer = source.getInventory(BillingClient.ProductType.INAPP, listOf("course.absent")).test()
        val callback = argumentCaptor<ProductDetailsResponseListener>()
        verify(client).queryProductDetailsAsync(any(), callback.capture())
        callback.firstValue.onProductDetailsResponse(BillingFixtures.result(), QueryProductDetailsResult.create(emptyList(), emptyList()))
        scheduler.triggerActions()
        observer.assertResult(emptyList())
    }

    @Test fun inventoryFailurePreservesResultAndWaitsForScheduler() {
        val result = BillingFixtures.result(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
        val observer = source.getInventory(BillingClient.ProductType.INAPP, listOf("course.a")).test()
        val callback = argumentCaptor<ProductDetailsResponseListener>()
        verify(client).queryProductDetailsAsync(any(), callback.capture())
        callback.firstValue.onProductDetailsResponse(result, QueryProductDetailsResult.create(emptyList(), emptyList()))
        observer.assertNoErrors().assertNotComplete()
        scheduler.triggerActions()
        observer.assertError { it is BillingNotSupportedException && it.code == result.responseCode && it.debugMessage == result.debugMessage }.assertNoValues()
    }

    @Test fun purchasesForwardTypeAndPreservePurchasedAndPendingItemsWithoutScheduler() {
        for (type in listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS)) {
            val observer = source.getAllPurchases(type).test()
            val params = argumentCaptor<QueryPurchasesParams>()
            val callback = argumentCaptor<PurchasesResponseListener>()
            verify(client).queryPurchasesAsync(params.capture(), callback.capture())
            assertEquals(type, params.firstValue.zza())
            observer.assertNoValues().assertNotComplete()
            val purchases = listOf(BillingFixtures.purchase(), BillingFixtures.purchase(state = Purchase.PurchaseState.PENDING))
            callback.firstValue.onQueryPurchasesResponse(BillingFixtures.result(), purchases)
            observer.assertResult(purchases)
            clearInvocations(client)
        }
    }

    @Test fun emptyPurchasesSucceed() {
        val observer = source.getAllPurchases(BillingClient.ProductType.INAPP).test()
        val callback = argumentCaptor<PurchasesResponseListener>()
        verify(client).queryPurchasesAsync(any<QueryPurchasesParams>(), callback.capture())
        callback.firstValue.onQueryPurchasesResponse(BillingFixtures.result(), emptyList())
        observer.assertResult(emptyList())
    }

    @Test fun purchasesFailurePreservesResult() {
        val result = BillingFixtures.result(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
        val observer = source.getAllPurchases(BillingClient.ProductType.INAPP).test()
        val callback = argumentCaptor<PurchasesResponseListener>()
        verify(client).queryPurchasesAsync(any<QueryPurchasesParams>(), callback.capture())
        callback.firstValue.onQueryPurchasesResponse(result, emptyList())
        observer.assertError { it is BillingNotSupportedException && it.code == result.responseCode && it.debugMessage == result.debugMessage }.assertNoValues()
    }

    @Test fun consumeUsesTokenAndWaitsForCallbackAndScheduler() {
        val purchase = BillingFixtures.purchase()
        val observer = source.consumePurchase(purchase).test()
        val params = argumentCaptor<ConsumeParams>()
        val callback = argumentCaptor<ConsumeResponseListener>()
        verify(client).consumeAsync(params.capture(), callback.capture())
        assertEquals(purchase.purchaseToken, params.firstValue.purchaseToken)
        assertNotEquals(purchase.orderId, params.firstValue.purchaseToken)
        scheduler.triggerActions()
        observer.assertNotComplete()
        callback.firstValue.onConsumeResponse(BillingFixtures.result(), "different-callback-token")
        observer.assertNotComplete()
        scheduler.triggerActions()
        observer.assertComplete().assertNoErrors()
    }

    @Test fun consumeFailurePreservesResultAndWaitsForScheduler() {
        val result = BillingFixtures.result(BillingClient.BillingResponseCode.DEVELOPER_ERROR)
        val observer = source.consumePurchase(BillingFixtures.purchase()).test()
        val callback = argumentCaptor<ConsumeResponseListener>()
        verify(client).consumeAsync(any(), callback.capture())
        callback.firstValue.onConsumeResponse(result, "synthetic-token")
        observer.assertNoErrors().assertNotComplete()
        scheduler.triggerActions()
        observer.assertError { it is BillingNotSupportedException && it.code == result.responseCode && it.debugMessage == result.debugMessage }
    }
}
