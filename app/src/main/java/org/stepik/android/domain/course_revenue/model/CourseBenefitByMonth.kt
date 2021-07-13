package org.stepik.android.domain.course_revenue.model

import com.google.gson.annotations.SerializedName
import java.util.Date

class CourseBenefitByMonth(
    @SerializedName("id")
    val id: String,
    @SerializedName("user")
    val user: Long,
    @SerializedName("date")
    val date: Date,
    @SerializedName("year")
    val year: Int,
    @SerializedName("month")
    val month: Int,
    @SerializedName("course_payments")
    val coursePayments: Int,
    @SerializedName("count_z_payments")
    val countZPayments: Int,
    @SerializedName("currency_code")
    val currencyCode: String,
    @SerializedName("total_turnover")
    val totalTurnover: String,
    @SerializedName("total_user_income")
    val totalUserIncome: String
)