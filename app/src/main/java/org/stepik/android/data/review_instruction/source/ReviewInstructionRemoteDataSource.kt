package org.stepik.android.data.review_instruction.source

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import io.reactivex.Single

interface ReviewInstructionRemoteDataSource {
    fun getReviewInstruction(id: Long): Single<ReviewInstruction>
}