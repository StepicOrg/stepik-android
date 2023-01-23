package org.stepik.android.domain.billing.exception

import com.android.billingclient.api.BillingResult

class BillingNotSupportedException(
    val code: Int,
    val debugMessage: String
) : Exception("Billing request error code = $code, message = $debugMessage") {
    constructor(billingResult: BillingResult) : this(billingResult.responseCode, billingResult.debugMessage)
}