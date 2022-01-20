package org.stepik.android.domain.course_revenue.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class CourseBenefit(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("buyer")
    val buyer: Long?,
    @SerializedName("course")
    val course: Long,
    @SerializedName("time")
    val time: Date,
    @SerializedName("status")
    val status: Status,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("payment_amount")
    val paymentAmount: String?,
    @SerializedName("currency_code")
    val currencyCode: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("is_z_link_used")
    val isZLinkUsed: Boolean?,
    @SerializedName("is_invoice_payment")
    val isInvoicePayment: Boolean,
    @SerializedName("seats_count")
    val seatsCount: Int,
    @SerializedName("promo_code")
    val promoCode: String?
) : Parcelable {
    enum class Status(val status: String) {
        @SerializedName("debited")
        DEBITED("debited"),
        @SerializedName("refunded")
        REFUNDED("refunded")
    }
    @IgnoredOnParcel
    val isManual: Boolean =
        buyer == null && !isInvoicePayment
}