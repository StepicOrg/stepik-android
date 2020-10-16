package org.stepik.android.remote.review.model

import org.stepik.android.domain.review.model.Review
import com.google.gson.annotations.SerializedName

class ReviewRequest(
    @SerializedName("review")
    val review: Review
)