package org.stepik.android.domain.course_revenue.model

import com.google.gson.annotations.SerializedName

data class CourseBeneficiary(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("percent")
    val percent: String,
    @SerializedName("is_valid")
    val isValid: Boolean
)