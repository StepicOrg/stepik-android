package org.stepik.android.domain.course_purchase.model

import com.android.billingclient.api.SkuDetails
import org.stepik.android.domain.course.model.CoursePurchasePayload

data class PurchaseFlowData(
    val coursePurchasePayload: CoursePurchasePayload,
    val obfuscatedParams: CoursePurchaseObfuscatedParams,
    val skuDetails: SkuDetails
)
