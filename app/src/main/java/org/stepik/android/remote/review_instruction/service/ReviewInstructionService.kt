package org.stepik.android.remote.review_instruction.service

import org.stepik.android.remote.review_instruction.model.ReviewInstructionResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ReviewInstructionService {
    @GET("api/instructions")
    fun getReviewInstructions(@Query("ids[]") ids: List<Long>): Single<ReviewInstructionResponse>
}