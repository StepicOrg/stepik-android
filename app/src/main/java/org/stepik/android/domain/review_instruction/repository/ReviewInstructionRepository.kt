package org.stepik.android.domain.review_instruction.repository

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType

interface ReviewInstructionRepository {
    fun getReviewInstruction(
        id: Long,
        primarySourceType: DataSourceType = DataSourceType.REMOTE
    ): Single<ReviewInstruction>

}