package org.stepik.android.data.review_instruction.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData

interface ReviewInstructionCacheDataSource {
    fun getReviewInstruction(id: Long): Maybe<ReviewInstructionData>

    fun saveReviewInstruction(item: ReviewInstructionData): Completable
}