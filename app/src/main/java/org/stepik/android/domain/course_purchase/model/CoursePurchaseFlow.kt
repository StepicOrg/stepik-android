package org.stepik.android.domain.course_purchase.model

import java.util.EnumSet

/**
 * IAP - only possible to purchase through IAP, if tier is null, show error message with info
 * WEB - old purchase flow through in-app web view or outside browser
 * IAP_FALLBACK_WEB - if tier is not null, use new purchase flow, otherwise use old purchase flow
 */
enum class CoursePurchaseFlow {
    IAP,
    WEB,
    IAP_FALLBACK_WEB;

    fun isInAppActive(): Boolean =
        this in EnumSet.of(IAP, IAP_FALLBACK_WEB)

    companion object {
        fun valueOfWithFallback(value: String): CoursePurchaseFlow =
            try {
                valueOf(value)
            } catch (exception: IllegalArgumentException) {
                WEB
            }
    }
}