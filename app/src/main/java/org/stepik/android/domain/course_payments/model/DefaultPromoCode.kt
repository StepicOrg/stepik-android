package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class DefaultPromoCode(
    val defaultPromoCodeName: String,
    val defaultPromoCodePrice: String,
    val defaultPromoCodeDiscount: String,
    val defaultPromoCodeExpireDate: Date?
) : Parcelable {
    companion object {
        val EMPTY = DefaultPromoCode("", "", "", null)
    }
}
