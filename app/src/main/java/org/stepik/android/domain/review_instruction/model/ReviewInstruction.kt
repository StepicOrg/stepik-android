package org.stepik.android.domain.review_instruction.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.ReviewStrategyType

@Entity
data class ReviewInstruction(
    @PrimaryKey
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