package org.stepik.android.domain.review_instruction.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.ReviewStrategyType
import ru.nobird.android.core.model.Identifiable

@Entity
data class ReviewInstruction(
    @PrimaryKey
    @SerializedName("id")
    override val id: Long,
    @SerializedName("step")
    val step: Long,
    @SerializedName("min_reviews")
    val minReviews: Int,
    @SerializedName("strategy_type")
    val strategyType: ReviewStrategyType,
    @SerializedName("rubrics")
    val rubrics: List<Long>,
    @SerializedName("is_frozen")
    val isFrozen: Boolean,
    @SerializedName("text")
    val text: String
) : Identifiable<Long>