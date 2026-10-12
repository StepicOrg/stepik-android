package org.stepik.android.testutil

import com.android.billingclient.api.BillingClient
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.stepik.android.remote.billing.BillingRemoteDataSourceImpl

class BillingBuilderSmokeTest {
    @get:Rule
    val rx = RxTestRule()

    @Test
    fun realInventoryBuilderAndCallbackUseInjectedScheduler() {
        val client = mock<BillingClient>()
        val callbacks = ControlledBillingCallbacks(client)
        val source = BillingRemoteDataSourceImpl(client, rx.scheduler)
        val products = listOf(BillingFixtures.product())
        val observer = rx.track(source.getInventory(BillingClient.ProductType.INAPP, listOf(BillingFixtures.PRODUCT_ID)).test())

        observer.assertNoErrors().assertNotComplete().assertNoValues()
        assertEquals(1, callbacks.inventory.count)
        callbacks.inventory.lastListener.onProductDetailsResponse(BillingFixtures.result(), BillingFixtures.inventory(products))
        observer.assertNoValues().assertNotComplete()
        rx.advance()
        observer.assertResult(products)
        assertEquals(emptyList<Throwable>(), rx.errors)
    }

    @Test
    fun realPurchaseAndConsumeBuildersWaitForCallbacks() {
        val client = mock<BillingClient>()
        val callbacks = ControlledBillingCallbacks(client)
        val source = BillingRemoteDataSourceImpl(client, rx.scheduler)
        val purchase = BillingFixtures.purchase()
        val purchases = rx.track(source.getAllPurchases(BillingClient.ProductType.INAPP).test())
        purchases.assertNoErrors().assertNotComplete()
        assertEquals(1, callbacks.purchases.count)
        callbacks.purchases.lastListener.onQueryPurchasesResponse(BillingFixtures.result(), listOf(purchase))
        purchases.assertResult(listOf(purchase))

        val consume = rx.track(source.consumePurchase(purchase).test())
        consume.assertNoErrors().assertNotComplete()
        assertEquals(BillingFixtures.PURCHASE_TOKEN, callbacks.consume.lastParams.purchaseToken)
        callbacks.consume.lastListener.onConsumeResponse(BillingFixtures.result(), BillingFixtures.PURCHASE_TOKEN)
        consume.assertNotComplete()
        rx.advance()
        consume.assertComplete().assertNoErrors()
        assertEquals(emptyList<Throwable>(), rx.errors)
    }
}
