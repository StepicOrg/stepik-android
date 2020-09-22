package org.stepik.android.cache.review_instruction

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.review_instruction.dao.ReviewInstructionDao
import org.stepik.android.data.review_instruction.source.ReviewInstructionCacheDataSource
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import javax.inject.Inject

class ReviewInstructionCacheDataSourceImpl
@Inject
constructor(
    private val reviewInstructionDao: ReviewInstructionDao
) : ReviewInstructionCacheDataSource {
    override fun getReviewInstruction(id: Long): Maybe<ReviewInstruction> =
        reviewInstructionDao
            .getReviewInstruction(id)

    override fun saveReviewInstruction(item: ReviewInstruction): Completable =
        reviewInstructionDao.saveReviewInstruction(item)
}