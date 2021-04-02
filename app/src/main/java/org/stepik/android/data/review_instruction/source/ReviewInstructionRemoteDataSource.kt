package org.stepik.android.data.review_instruction.source

import io.reactivex.Single
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData

interface ReviewInstructionRemoteDataSource {
    fun getReviewInstruction(id: Long): Single<ReviewInstructionData>
}