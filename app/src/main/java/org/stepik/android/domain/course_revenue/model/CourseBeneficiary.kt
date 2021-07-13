package org.stepik.android.domain.course_revenue.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
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
) : Parcelable {
    companion object {
        val EMPTY = CourseBeneficiary(-1L, -1L, -1L, "", false)
    }
}