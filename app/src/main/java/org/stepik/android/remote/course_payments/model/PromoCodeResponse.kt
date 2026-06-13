package org.stepik.android.remote.course_payments.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PromoCodeResponse(
    @SerializedName("price")
    val price: String,
    @SerializedName("currency_code")
    val currencyCode: String
) : Parcelable