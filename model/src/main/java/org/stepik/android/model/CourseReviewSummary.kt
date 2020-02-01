package org.stepik.android.model

import com.google.gson.annotations.SerializedName

data class CourseReviewSummary(
    @SerializedName("id")
    val id: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("average")
    val average: Double,
    @SerializedName("count")
    val count: Int,
    @SerializedName("distribution")
    val distribution: List<Long>
)