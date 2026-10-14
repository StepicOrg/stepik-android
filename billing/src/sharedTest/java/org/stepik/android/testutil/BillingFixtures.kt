package org.stepik.android.testutil

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock

object BillingFixtures {
    fun result(code: Int = BillingClient.BillingResponseCode.OK): BillingResult =
        BillingResult.newBuilder().setResponseCode(code).setDebugMessage("synthetic billing result $code").build()

    fun product(id: String = "course.synthetic", micros: Long = 12_340_000, currency: String = "EUR", hasOffer: Boolean = true): ProductDetails {
        val offer = if (hasOffer) mock<ProductDetails.OneTimePurchaseOfferDetails> {
            on { priceAmountMicros } doReturn micros
            on { priceCurrencyCode } doReturn currency
            on { formattedPrice } doReturn "12,34 €"
        } else null
        return mock {
            on { productId } doReturn id
            on { oneTimePurchaseOfferDetails } doReturn offer
        }
    }

    fun purchase(products: List<String> = listOf("course.synthetic"), state: Int = Purchase.PurchaseState.PURCHASED): Purchase = mock {
        on { this.products } doReturn products
        on { purchaseState } doReturn state
        on { purchaseToken } doReturn "synthetic-token"
        on { orderId } doReturn "synthetic-order"
        on { packageName } doReturn "org.example.synthetic"
    }
}
