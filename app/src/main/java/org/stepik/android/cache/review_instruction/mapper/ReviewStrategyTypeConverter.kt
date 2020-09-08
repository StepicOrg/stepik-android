package org.stepik.android.cache.review_instruction.mapper

import androidx.room.TypeConverter
import org.stepik.android.model.ReviewStrategyType

class ReviewStrategyTypeConverter {
    @TypeConverter
    fun reviewStrategyTypeToInt(value: ReviewStrategyType?): Int =
        value?.ordinal ?: -1

    @TypeConverter
    fun intToReviewStrategyType(value: Int): ReviewStrategyType? =
        ReviewStrategyType.values().getOrNull(value)
}