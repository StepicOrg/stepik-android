package org.stepik.android.testutil

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever

class ControlledBillingCallbacks(client: BillingClient) {
    val inventory = CallbackCalls<QueryProductDetailsParams, ProductDetailsResponseListener>()
    val purchases = CallbackCalls<QueryPurchasesParams, PurchasesResponseListener>()
    val consume = CallbackCalls<ConsumeParams, ConsumeResponseListener>()

    init {
        doAnswer {
            inventory.capture(it.getArgument(0), it.getArgument(1))
            null
        }.whenever(client).queryProductDetailsAsync(any(), any())
        doAnswer {
            purchases.capture(it.getArgument(0), it.getArgument(1))
            null
        }.whenever(client).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
        doAnswer {
            consume.capture(it.getArgument(0), it.getArgument(1))
            null
        }.whenever(client).consumeAsync(any(), any())
    }
}

class CallbackCalls<P, L> {
    private val calls = mutableListOf<Pair<P, L>>()
    val count: Int get() = calls.size
    val lastParams: P get() = calls.last().first
    val lastListener: L get() = calls.last().second

    fun capture(params: P, listener: L) {
        calls += params to listener
    }
}
