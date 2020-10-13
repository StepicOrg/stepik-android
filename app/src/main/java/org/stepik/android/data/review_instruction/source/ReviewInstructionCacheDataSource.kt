package org.stepik.android.data.review_instruction.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.review_instruction.model.ReviewInstruction

interface ReviewInstructionCacheDataSource {
    fun getReviewInstruction(id: Long): Maybe<ReviewInstruction>

    fun saveReviewInstruction(item: ReviewInstruction): Completable
}