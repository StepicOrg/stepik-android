package org.stepik.android.cache.review_instruction

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.cache.review_instruction.dao.ReviewInstructionDao
import org.stepik.android.cache.rubric.dao.RubricDao
import org.stepik.android.data.review_instruction.source.ReviewInstructionCacheDataSource
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData
import javax.inject.Inject

class ReviewInstructionCacheDataSourceImpl
@Inject
constructor(
    private val reviewInstructionDao: ReviewInstructionDao,
    private val rubricDao: RubricDao
) : ReviewInstructionCacheDataSource {
    override fun getReviewInstruction(id: Long): Maybe<ReviewInstructionData> =
        reviewInstructionDao
            .getReviewInstruction(id)
            .map { reviewInstruction ->
                val rubrics = rubricDao.getRubrics(reviewInstruction.rubrics)
                ReviewInstructionData(reviewInstruction, rubrics)
            }

    override fun saveReviewInstruction(item: ReviewInstructionData): Completable =
        reviewInstructionDao.saveReviewInstruction(item.reviewInstruction)
            .doOnComplete { rubricDao.saveRubrics(item.rubrics) }
}