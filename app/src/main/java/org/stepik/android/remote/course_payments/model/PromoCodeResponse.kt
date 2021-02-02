package org.stepik.android.remote.course_payments.model

import com.google.gson.annotations.SerializedName

class PromoCodeResponse(
    @SerializedName("price")
    val price: Long,
    @SerializedName("currency_code")
    val currencyCode: String
)