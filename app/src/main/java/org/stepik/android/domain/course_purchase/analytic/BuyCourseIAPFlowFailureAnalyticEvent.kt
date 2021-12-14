package org.stepik.android.domain.course_purchase.analytic

import com.android.billingclient.api.BillingClient
import org.stepik.android.domain.base.analytic.AnalyticEvent

class BuyCourseIAPFlowFailureAnalyticEvent(
    courseId: Long,
    responseCode: Int,
    message: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TYPE = "type"
        private const val PARAM_MESSAGE = "message"
    }
    override val name: String =
        "Buy course IAP flow failure"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_TYPE to mapResponseCodeToType(responseCode),
            PARAM_MESSAGE to message
        )

    private fun mapResponseCodeToType(responseCode: Int): String =
        when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT ->
                "Service timeout"
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                "Feature not supported"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
                "Service disconnected"
            BillingClient.BillingResponseCode.OK ->
                "OK"
            BillingClient.BillingResponseCode.USER_CANCELED ->
                "User cancelled"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                "Service unavailable"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                "Billing unavailable"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                "Item unavailable"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                "Developer error"
            BillingClient.BillingResponseCode.ERROR ->
                "Error"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                "Item already owned"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
                "Item not owned"
            else ->
                "Unknown error"
        }
}