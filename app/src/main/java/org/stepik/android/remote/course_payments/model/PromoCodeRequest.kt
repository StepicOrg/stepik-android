package org.stepik.android.remote.course_payments.model

import com.google.gson.annotations.SerializedName

class PromoCodeRequest(
    @SerializedName("course")
    val course: Long,
    @SerializedName("name")
    val name: String
)