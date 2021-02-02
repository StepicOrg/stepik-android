package org.stepik.android.domain.course_payments.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class PromoCode(
    @SerializedName("price")
    val price: Long,
    @SerializedName("currency_code")
    val currencyCode: String
) : Parcelable