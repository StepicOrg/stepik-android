package org.stepik.android.domain.course.model

/**
 * PURCHASE_FLOW_IAP - only possible to purchase through IAP, if tier is null, show error message with info
 * PURCHASE_FLOW_WEB - old purchase flow through in-app web view or outside browser
 * PURCHASE_FLOW_IAP_FALLBACK_WEB - if tier is not null, use new purchase flow, otherwise use old purchase flow
 */
object CoursePurchaseFlow {
    const val PURCHASE_FLOW_IAP = "iap"
    const val PURCHASE_FLOW_WEB = "web"
    const val PURCHASE_FLOW_IAP_FALLBACK_WEB = "iap_fallback_web"
}