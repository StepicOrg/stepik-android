package org.stepik.android.remote.review_instruction.model

import org.stepik.android.model.ReviewInstruction
import com.google.gson.annotations.SerializedName

class ReviewInstructionResponse(
    @SerializedName("instructions")
    val instructions: List<ReviewInstruction>
)