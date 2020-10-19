package org.stepik.android.remote.review_instruction.model

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import com.google.gson.annotations.SerializedName

class ReviewInstructionResponse(
    @SerializedName("instructions")
    val instructions: List<ReviewInstruction>
)