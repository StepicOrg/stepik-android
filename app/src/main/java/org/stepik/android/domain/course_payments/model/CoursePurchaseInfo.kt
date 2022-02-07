package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams

sealed class CoursePurchaseInfo : Parcelable {
    @Parcelize
    object Unavailable : CoursePurchaseInfo()

    @Parcelize
    data class Empty(val obfuscatedParams: CoursePurchaseObfuscatedParams) : CoursePurchaseInfo()

    @Parcelize
    data class Result(val obfuscatedParams: CoursePurchaseObfuscatedParams, val purchaseState: Int) : CoursePurchaseInfo()
}