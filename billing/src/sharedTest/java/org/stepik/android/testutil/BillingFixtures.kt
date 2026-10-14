package org.stepik.android.testutil

import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.UnfetchedProduct
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock

/** Synthetic accessor mocks: no Play Store, real purchase JSON, or Android runtime required. */
object BillingFixtures {
    const val PRODUCT_ID = "test.course.101"
    const val PURCHASE_TOKEN = "synthetic-token"
    const val ORDER_ID = "synthetic-order"
    const val ACCOUNT_ID = "test-account-hash"
    const val PROFILE_ID = "test-course-hash"

    fun offer(
        priceMicros: Long = 4_990_000L,
        formattedPrice: String = "$4.99",
        currency: String = "USD"
    ): ProductDetails.OneTimePurchaseOfferDetails = mock {
        on { priceAmountMicros } doReturn priceMicros
        on { this.formattedPrice } doReturn formattedPrice
        on { priceCurrencyCode } doReturn currency
    }

    // Bare products use the newer $4.99 fixture; parameterized products retain legacy price defaults.
    fun product(): ProductDetails = product(id = PRODUCT_ID, offer = offer())

    fun product(
        id: String = PRODUCT_ID,
        micros: Long = 12_340_000L,
        currency: String = "EUR",
        hasOffer: Boolean = true,
        offer: ProductDetails.OneTimePurchaseOfferDetails? = offer(
            priceMicros = micros,
            formattedPrice = "12,34 €",
            currency = currency
        )
    ): ProductDetails = mock {
        on { productId } doReturn id
        on { productType } doReturn BillingClient.ProductType.INAPP
        on { oneTimePurchaseOfferDetails } doReturn if (hasOffer) offer else null
    }

    fun identifiers(
        accountId: String? = ACCOUNT_ID,
        profileId: String? = PROFILE_ID
    ): AccountIdentifiers = mock {
        on { obfuscatedAccountId } doReturn accountId
        on { obfuscatedProfileId } doReturn profileId
    }

    fun purchase(
        products: List<String> = listOf(PRODUCT_ID),
        state: Int = Purchase.PurchaseState.PURCHASED,
        token: String = PURCHASE_TOKEN,
        orderId: String? = ORDER_ID,
        identifiers: AccountIdentifiers? = identifiers()
    ): Purchase = mock {
        on { this.products } doReturn products
        on { purchaseToken } doReturn token
        on { this.orderId } doReturn orderId
        on { accountIdentifiers } doReturn identifiers
        on { purchaseState } doReturn state
        on { packageName } doReturn "org.example.synthetic"
    }

    fun result(code: Int = BillingClient.BillingResponseCode.OK): BillingResult =
        BillingResult.newBuilder().setResponseCode(code).setDebugMessage("Synthetic billing result").build()

    fun inventory(
        products: List<ProductDetails> = emptyList(),
        unfetched: List<UnfetchedProduct> = emptyList()
    ): QueryProductDetailsResult = QueryProductDetailsResult.create(products, unfetched)
}
