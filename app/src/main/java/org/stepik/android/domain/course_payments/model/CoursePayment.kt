package org.stepik.android.domain.course_payments.model

import com.google.gson.annotations.SerializedName

class CoursePayment(
    @SerializedName("id")
    val id: Long,

    @SerializedName("course")
    val course: Long,

    @SerializedName("is_paid")
    val isPaid: Boolean,

    @SerializedName("status")
    val status: Status,

    @SerializedName("user")
    val user: Long
) {
    /**
     * CoursePaymentStatus
     */
    enum class Status {
        @SerializedName("pending")
        PENDING,

        @SerializedName("amount_blocked")
        AMOUNT_BLOCKED,

        @SerializedName("success")
        SUCCESS,

        @SerializedName("canceled")
        CANCELED,

        @SerializedName("failed")
        FAILED,

        @SerializedName("expired")
        EXPIRED
    }
}