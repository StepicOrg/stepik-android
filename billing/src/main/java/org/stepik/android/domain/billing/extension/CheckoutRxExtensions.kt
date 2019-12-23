package org.stepik.android.domain.billing.extension

import io.reactivex.Completable
import io.reactivex.Single
import org.solovyev.android.checkout.BillingRequests
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.RequestListener
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout

fun UiCheckout.startPurchaseFlowRx(sku: Sku, payload: String?): Single<Purchase> =
    Single.create { emitter ->
        startPurchaseFlow(sku, payload, object : RequestListener<Purchase> {
            override fun onSuccess(purchase: Purchase) {
                if (!emitter.isDisposed) {
                    emitter.onSuccess(purchase)
                }
            }

            override fun onError(response: Int, exception: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(exception)
                }
            }
        })
    }

fun Checkout.onReady(): Single<BillingRequests> =
    Single.create { emitter ->
        whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                if (!emitter.isDisposed) {
                    emitter.onSuccess(requests)
                }
            }
        })
    }

fun BillingRequests.consumeRx(token: String): Completable =
    Completable.create { emitter ->
        consume(token, object : RequestListener<Any> {
            override fun onSuccess(result: Any) {
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }

            override fun onError(response: Int, exception: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(exception)
                }
            }
        })
    }