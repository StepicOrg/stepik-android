package org.stepik.android.testutil

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryProductDetailsResult
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.stepik.android.remote.billing.BillingRemoteDataSourceImpl

class BillingInfrastructureTest {
    @Test
    fun finalSdkAccessorsAndExistingKotlinWrapperWork() {
        val product = BillingFixtures.product()
        val queryResult = mock<QueryProductDetailsResult>()
        whenever(queryResult.productDetailsList) doReturn listOf(product)

        assertSame(product, queryResult.productDetailsList.single())
        assertEquals(BillingFixtures.PRODUCT_ID, product.productId)
        assertEquals(4_990_000L, product.oneTimePurchaseOfferDetails!!.priceAmountMicros)
        assertEquals(BillingFixtures.ACCOUNT_ID, BillingFixtures.identifiers().obfuscatedAccountId)
    }

    @Test
    fun finalKotlinCollaboratorCanBeStubbedVerifiedAndCaptured() {
        val source = mock<BillingRemoteDataSourceImpl>()
        val products = listOf(BillingFixtures.product())
        whenever(source.getInventory(any(), any())) doReturn Single.just(products)

        source.getInventory(BillingClient.ProductType.INAPP, listOf(BillingFixtures.PRODUCT_ID))
            .test().assertResult(products)

        val ids = argumentCaptor<List<String>>()
        verify(source).getInventory(com.nhaarman.mockitokotlin2.eq(BillingClient.ProductType.INAPP), ids.capture())
        assertEquals(listOf(BillingFixtures.PRODUCT_ID), ids.firstValue)
    }

    @Test
    fun syntheticFixturesSupportIncompleteBillingResponses() {
        val product = BillingFixtures.product(offer = null)
        val purchase = BillingFixtures.purchase(orderId = null, identifiers = null, products = emptyList())
        assertNull(product.oneTimePurchaseOfferDetails)
        assertNull(purchase.orderId)
        assertNull(purchase.accountIdentifiers)
        assertEquals(emptyList<String>(), purchase.products)
        assertEquals(emptyList<com.android.billingclient.api.ProductDetails>(), BillingFixtures.inventory().productDetailsList)
        assertEquals(listOf(product), BillingFixtures.inventory(listOf(product)).productDetailsList)
    }
}
