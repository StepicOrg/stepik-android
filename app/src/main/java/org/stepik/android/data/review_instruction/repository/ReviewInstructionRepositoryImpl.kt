package org.stepik.android.data.review_instruction.repository

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.data.review_instruction.source.ReviewInstructionCacheDataSource
import org.stepik.android.data.review_instruction.source.ReviewInstructionRemoteDataSource
import org.stepik.android.domain.review_instruction.repository.ReviewInstructionRepository
import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.SingleRepositoryDelegate
import org.stepik.android.domain.base.DataSourceType
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ReviewInstructionRepositoryImpl
@Inject
constructor(
    private val reviewInstructionCacheDataSource: ReviewInstructionCacheDataSource,
    private val reviewInstructionRemoteDataSource: ReviewInstructionRemoteDataSource
) : ReviewInstructionRepository {
    private val delegate =
        SingleRepositoryDelegate(
            reviewInstructionRemoteDataSource::getReviewInstruction,
            reviewInstructionCacheDataSource::getReviewInstruction,
            reviewInstructionCacheDataSource::saveReviewInstruction
        )

    override fun getReviewInstruction(
        id: Long,
        primarySourceType: DataSourceType
    ): Single<ReviewInstruction> =
        delegate.get(id, primarySourceType, allowFallback = true)
}