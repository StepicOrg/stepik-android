package org.stepik.android.model

import com.google.gson.annotations.SerializedName

data class ReviewInstruction(
    @SerializedName("id")
    val id: Long,
    @SerializedName("step")
    val step: Long,
    @SerializedName("min_reviews")
    val minReviews: Long,
    @SerializedName("strategy_type")
    val strategyType: ReviewStrategyType,
    @SerializedName("rubrics")
    val rubrics: List<Long>,
    @SerializedName("is_frozen")
    val isFrozen: Boolean,
    @SerializedName("text")
    val text: String
)