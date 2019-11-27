package org.stepik.android.remote.recommendation.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.adaptive.Recommendation

class RecommendationsResponse(
    @SerializedName("recommendations")
    val recommendations: List<Recommendation>?
)