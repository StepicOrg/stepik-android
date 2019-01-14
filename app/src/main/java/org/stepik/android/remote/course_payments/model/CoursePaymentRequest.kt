package org.stepik.android.remote.course_payments.model

import com.google.gson.annotations.SerializedName

class CoursePaymentRequest(
    @SerializedName("course")
    val course: Long,

    @SerializedName("provider")
    val provider: Provider,

    @SerializedName("data")
    val data: Data
) {
    enum class Provider {
        @SerializedName("Google")
        GOOGLE
    }

    class Data(
        @SerializedName("token")
        val token: String,

        @SerializedName("package_name")
        val packageName: String,

        @SerializedName("product_id")
        val productId: String,

        @SerializedName("amount")
        val amount: Float,

        @SerializedName("currency")
        val currency: String
    )
}