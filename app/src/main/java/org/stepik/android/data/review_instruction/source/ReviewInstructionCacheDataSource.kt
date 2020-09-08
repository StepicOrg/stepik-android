package org.stepik.android.data.review_instruction.source

import org.stepik.android.model.ReviewInstruction
import io.reactivex.Completable
import io.reactivex.Single

interface ReviewInstructionCacheDataSource {
    fun getReviewInstruction(id: Long): Single<ReviewInstruction>

    fun saveReviewInstruction(item: ReviewInstruction): Completable
}