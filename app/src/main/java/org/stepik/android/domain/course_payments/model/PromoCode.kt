package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PromoCode(
    @SerializedName("price")
    val price: String,
    @SerializedName("currency_code")
    val currencyCode: String
) : Parcelable {
    companion object {
        val EMPTY = PromoCode("", "")
    }
}