package org.stepik.android.remote.billing

import com.android.billingclient.api.*
import com.nhaarman.mockitokotlin2.*
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.stepik.android.domain.billing.exception.BillingNotSupportedException
import org.stepik.android.testutil.BillingFixtures
import org.stepik.android.testutil.RxErrorsRule

@RunWith(Parameterized::class)
class BillingCallbackLifecycleTest(private val operation: String) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun operations() = listOf(arrayOf("inventory"), arrayOf("purchases"), arrayOf("consume"))
    }

    @get:Rule val rxErrors = RxErrorsRule()
    private val client = mock<BillingClient>()
    private val scheduler = TestScheduler()
    private val source = BillingRemoteDataSourceImpl(client, scheduler)
    private lateinit var reply: (BillingResult) -> Unit
    private val sdkFailure = IllegalStateException("synthetic synchronous SDK failure")

    private fun subscribe(throwBeforeCallback: Boolean = false, throwAfterCallback: Boolean = false): TestObserver<*> {
        val answer: (BillingResult) -> Unit = {
            if (throwBeforeCallback) throw sdkFailure
            if (throwAfterCallback) {
                reply(BillingFixtures.result())
                throw sdkFailure
            }
        }
        return when (operation) {
            "inventory" -> {
                doAnswer {
                    val listener = it.getArgument<ProductDetailsResponseListener>(1)
                    reply = { result -> listener.onProductDetailsResponse(result, QueryProductDetailsResult.create(emptyList(), emptyList())) }
                    answer(BillingFixtures.result())
                    null
                }.whenever(client).queryProductDetailsAsync(any(), any())
                source.getInventory(BillingClient.ProductType.INAPP, listOf("course.synthetic")).test()
            }
            "purchases" -> {
                doAnswer {
                    val listener = it.getArgument<PurchasesResponseListener>(1)
                    reply = { result -> listener.onQueryPurchasesResponse(result, emptyList()) }
                    answer(BillingFixtures.result())
                    null
                }.whenever(client).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
                source.getAllPurchases(BillingClient.ProductType.INAPP).test()
            }
            else -> {
                doAnswer {
                    val listener = it.getArgument<ConsumeResponseListener>(1)
                    reply = { result -> listener.onConsumeResponse(result, "synthetic-token") }
                    answer(BillingFixtures.result())
                    null
                }.whenever(client).consumeAsync(any(), any())
                source.consumePurchase(BillingFixtures.purchase()).test()
            }
        }
    }

    @Test fun disposedSuccessCallbackIsIgnored() {
        val observer = subscribe()
        observer.dispose()
        reply(BillingFixtures.result())
        scheduler.triggerActions()
        observer.assertNoValues().assertNoErrors().assertNotComplete()
    }

    @Test fun disposedErrorCallbackIsIgnoredWithoutGlobalErrors() {
        val observer = subscribe()
        observer.dispose()
        reply(BillingFixtures.result(BillingClient.BillingResponseCode.ERROR))
        scheduler.triggerActions()
        observer.assertNoValues().assertNoErrors().assertNotComplete()
    }

    @Test fun duplicateSuccessAndLateErrorKeepFirstSuccess() {
        val observer = subscribe()
        reply(BillingFixtures.result())
        reply(BillingFixtures.result())
        reply(BillingFixtures.result(BillingClient.BillingResponseCode.ERROR))
        scheduler.triggerActions()
        observer.assertComplete().assertNoErrors()
        if (operation != "consume") observer.assertValueCount(1)
    }

    @Test fun duplicateErrorAndLateSuccessKeepFirstError() {
        val observer = subscribe()
        reply(BillingFixtures.result(BillingClient.BillingResponseCode.ERROR))
        reply(BillingFixtures.result(BillingClient.BillingResponseCode.DEVELOPER_ERROR))
        reply(BillingFixtures.result())
        scheduler.triggerActions()
        observer.assertNoValues().assertNotComplete().assertError { it is BillingNotSupportedException && it.code == BillingClient.BillingResponseCode.ERROR }
    }

    @Test fun synchronousSdkThrowBecomesStreamError() {
        val observer = subscribe(throwBeforeCallback = true)
        if (operation != "purchases") observer.assertNoErrors()
        scheduler.triggerActions()
        observer.assertNoValues().assertNotComplete().assertError { it === sdkFailure }
        assertSame(sdkFailure, observer.errors().single())
    }

    @Test fun synchronousSdkThrowAfterCallbackKeepsFirstTerminalWithoutGlobalErrors() {
        val observer = subscribe(throwAfterCallback = true)
        scheduler.triggerActions()
        observer.assertComplete().assertNoErrors()
        if (operation != "consume") observer.assertValueCount(1)
    }

    @Test fun disposalCancelsQueuedDelivery() {
        val observer = subscribe()
        reply(BillingFixtures.result())
        observer.dispose()
        scheduler.triggerActions()
        if (operation == "purchases") {
            // This adapter intentionally delivers purchases directly on the callback thread.
            observer.assertValueCount(1).assertComplete().assertNoErrors()
        } else {
            observer.assertNoValues().assertNotComplete().assertNoErrors()
        }
    }

    @Test fun disposalCancelsQueuedErrors() {
        val observer = subscribe()
        reply(BillingFixtures.result(BillingClient.BillingResponseCode.ERROR))
        observer.dispose()
        scheduler.triggerActions()
        if (operation == "purchases") observer.assertError(BillingNotSupportedException::class.java)
        else observer.assertNoValues().assertNotComplete().assertNoErrors()
    }
}
