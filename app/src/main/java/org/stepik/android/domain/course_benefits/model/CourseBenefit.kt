package org.stepik.android.domain.course_benefits.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CourseBenefit(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("time")
    val time: Date,
    @SerializedName("status")
    val status: Status,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("currency_code")
    val currencyCode: String,
    @SerializedName("total_income")
    val totalIncome: String,
    @SerializedName("is_z_link_used")
    val isZLinkUsed: Boolean,
    @SerializedName("promo_code")
    val promoCode: String?
) {
    enum class Status(val status: String) {
        @SerializedName("debited")
        DEBITED("debited"),
        @SerializedName("refunded")
        REFUNDED("refunded")
    }
}