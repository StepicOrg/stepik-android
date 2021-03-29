package org.stepik.android.remote.review_instruction.model

import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.rubric.model.Rubric

class ReviewInstructionResponse(
    @SerializedName("instructions")
    val instructions: List<ReviewInstruction>,
    @SerializedName("rubrics")
    val rubrics: List<Rubric>
)