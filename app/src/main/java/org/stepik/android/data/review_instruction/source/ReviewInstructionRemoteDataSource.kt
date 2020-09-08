package org.stepik.android.data.review_instruction.source

import org.stepik.android.model.ReviewInstruction
import io.reactivex.Single

interface ReviewInstructionRemoteDataSource {
    fun getReviewInstruction(id: Long): Single<ReviewInstruction>
}