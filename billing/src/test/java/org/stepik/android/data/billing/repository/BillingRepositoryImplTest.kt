package org.stepik.android.data.billing.repository

import com.android.billingclient.api.BillingClient
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.testutil.BillingFixtures

class BillingRepositoryImplTest {
    private val source = mock<BillingRemoteDataSource>()
    private val repository = BillingRepositoryImpl(source)
    private val type = BillingClient.ProductType.SUBS

    @Test fun forwardsArgumentsAndWaitsForSourceResults() {
        val ids = listOf("subscription.synthetic")
        val products = listOf(BillingFixtures.product(ids.single()))
        val purchase = BillingFixtures.purchase(ids)
        val inventory = SingleSubject.create<List<com.android.billingclient.api.ProductDetails>>()
        val purchases = SingleSubject.create<List<com.android.billingclient.api.Purchase>>()
        val consume = CompletableSubject.create()
        whenever(source.getInventory(type, ids)).thenReturn(inventory)
        whenever(source.getAllPurchases(type)).thenReturn(purchases)
        whenever(source.consumePurchase(purchase)).thenReturn(consume)
        val inventoryObserver = repository.getInventory(type, ids).test()
        val purchasesObserver = repository.getAllPurchases(type).test()
        val consumeObserver = repository.consumePurchase(purchase).test()
        inventoryObserver.assertNoValues().assertNotComplete()
        purchasesObserver.assertNoValues().assertNotComplete()
        consumeObserver.assertNotComplete()
        inventory.onSuccess(products)
        purchases.onSuccess(listOf(purchase))
        consume.onComplete()
        inventoryObserver.assertResult(products)
        purchasesObserver.assertResult(listOf(purchase))
        consumeObserver.assertComplete().assertNoErrors()
        verify(source).getInventory(type, ids)
        verify(source).getAllPurchases(type)
        verify(source).consumePurchase(purchase)
        verifyNoMoreInteractions(source)
    }

    @Test fun forwardsOriginalErrorsWithoutRetry() {
        val failure = IllegalStateException("synthetic source failure")
        val ids = listOf("subscription.synthetic")
        val purchase = BillingFixtures.purchase(ids)
        whenever(source.getInventory(type, ids)).thenReturn(Single.error(failure))
        whenever(source.getAllPurchases(type)).thenReturn(Single.error(failure))
        whenever(source.consumePurchase(purchase)).thenReturn(Completable.error(failure))
        repository.getInventory(type, ids).test().assertError { it === failure }.assertNoValues()
        repository.getAllPurchases(type).test().assertError { it === failure }.assertNoValues()
        repository.consumePurchase(purchase).test().assertError { it === failure }.assertNotComplete()
        verify(source).getInventory(type, ids)
        verify(source).getAllPurchases(type)
        verify(source).consumePurchase(purchase)
        verifyNoMoreInteractions(source)
    }
}
