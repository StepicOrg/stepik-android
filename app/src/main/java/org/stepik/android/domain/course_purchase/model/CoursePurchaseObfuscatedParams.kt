package org.stepik.android.domain.course_purchase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CoursePurchaseObfuscatedParams(
    val obfuscatedAccountId: String,
    val obfuscatedProfileId: String
) : Parcelable