package org.stepic.droid.util

import io.reactivex.Single
import org.solovyev.android.checkout.*

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