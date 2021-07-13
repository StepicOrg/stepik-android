package org.stepik.android.domain.course_revenue.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CourseBenefitByMonth(
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
    @SerializedName("count_payments")
    val countPayments: Int,
    @SerializedName("count_invoice_payments")
    val countInvoicePayments: Int,
    @SerializedName("count_z_payments")
    val countZPayments: Int,
    @SerializedName("count_non_z_payments")
    val countNonZPayments: Int,
    @SerializedName("count_refunds")
    val countRefunds: Int,
    @SerializedName("currency_code")
    val currencyCode: String,
    @SerializedName("total_turnover")
    val totalTurnover: String,
    @SerializedName("total_user_income")
    val totalUserIncome: String,
    @SerializedName("total_refunds")
    val totalRefunds: String
)