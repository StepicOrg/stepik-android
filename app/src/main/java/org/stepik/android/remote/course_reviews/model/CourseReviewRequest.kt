package org.stepik.android.remote.course_reviews.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.course_reviews.model.CourseReview

class CourseReviewRequest(
    @SerializedName("courseReview")
    val courseReview: CourseReview
)