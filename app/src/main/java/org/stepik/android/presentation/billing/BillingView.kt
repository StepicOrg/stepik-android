package org.stepik.android.presentation.billing

import org.solovyev.android.checkout.UiCheckout

interface BillingView {
    fun createUiCheckout(): UiCheckout
}