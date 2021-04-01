package org.stepik.android.domain.review_instruction.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData

interface ReviewInstructionRepository {
    fun getReviewInstruction(id: Long, sourceType: DataSourceType = DataSourceType.REMOTE): Single<ReviewInstructionData>
}