package org.stepik.android.remote.course_reviews.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.model.Meta

class CourseReviewsResponse(
    @SerializedName("meta")
    val meta: Meta,

    @SerializedName("course-reviews")
    val courseReviews: List<CourseReview>
)