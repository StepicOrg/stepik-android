package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class CourseReviewSummary(
    @SerializedName("course")
    val course: Long,
    @SerializedName("average")
    val average: Double
) {
    override fun toString(): String {
        return "CourseReviewSummary(course=$course, average=$average)"
    }
}