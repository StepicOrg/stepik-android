package org.stepik.android.domain.course_benefits.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CourseBenefitSummary(
    @SerializedName("id")
    val id: Long,
    @SerializedName("begin_payment_date")
    val beginPaymentDate: Date,
    @SerializedName("current_date")
    val currentDate: Date,
    @SerializedName("total_user_income")
    val totalUserIncome: String,
    @SerializedName("total_turnover")
    val totalTurnover: String,
    @SerializedName("month_user_income")
    val monthUserIncome: String,
    @SerializedName("month_turnover")
    val monthTurnover: String,
    @SerializedName("currency_code")
    val currencyCode: String
) {
    companion object {
        val EMPTY = CourseBenefitSummary(-1L, Date(0), Date(0), "", "", "", "", "")
    }
}