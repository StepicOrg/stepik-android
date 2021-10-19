package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeeplinkPromoCode(
    val name: String,
    val price: String,
    val currencyCode: String
) : Parcelable {
    companion object {
        val EMPTY = DeeplinkPromoCode("", "", "")
    }
}