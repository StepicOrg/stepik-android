package org.stepik.android.domain.course_purchase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoursePurchaseObfuscatedParams(
    val obfuscatedAccountId: String,
    val obfuscatedProfileId: String
) : Parcelable