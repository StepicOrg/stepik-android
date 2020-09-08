package org.stepik.android.cache.review_instruction.dao

import org.stepik.android.model.ReviewInstruction
import io.reactivex.Completable
import io.reactivex.Single

interface ReviewInstructionDao {
    fun getReviewInstruction(id: Long): Single<ReviewInstruction>

    fun saveReviewInstruction(item: ReviewInstruction): Completable
}