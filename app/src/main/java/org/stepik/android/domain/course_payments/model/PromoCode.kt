package org.stepik.android.domain.course_payments.model

import com.google.gson.annotations.SerializedName

class PromoCode(
    @SerializedName("price")
    val price: Long,
    @SerializedName("currency_code")
    val currencyCode: String
)