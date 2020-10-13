package org.stepik.android.remote.review.model

import org.stepik.android.domain.review.model.Review
import com.google.gson.annotations.SerializedName

class ReviewResponse(
    @SerializedName("reviews")
    val reviews: List<Review>
)