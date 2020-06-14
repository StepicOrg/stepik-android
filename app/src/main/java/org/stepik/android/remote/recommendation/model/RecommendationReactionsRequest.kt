package org.stepik.android.remote.recommendation.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.adaptive.RecommendationReaction

class RecommendationReactionsRequest(
    @SerializedName("recommendation_reaction")
    private val recommendationReaction: RecommendationReaction
)