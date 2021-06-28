package org.stepik.android.domain.course_benefits.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CourseBenefitSummary(
    @SerializedName("id")
    val id: Long,
    @SerializedName("being_payment_date")
    val beginPaymentDate: Date,
    @SerializedName("current_date")
    val currentDate: Date,
    @SerializedName("total_income")
    val totalIncome: String,
    @SerializedName("total_turnover")
    val totalTurnover: String,
    @SerializedName("total_user_income")
    val totalUserIncome: String,
    @SerializedName("month_income")
    val monthIncome: String,
    @SerializedName("month_turnover")
    val monthTurnover: String,
    @SerializedName("month_user_income")
    val monthUserIncome: String,
    @SerializedName("currency_code")
    val currencyCode: String
)