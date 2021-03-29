package org.stepik.android.domain.review_instruction.model

import org.stepik.android.domain.rubric.model.Rubric
import ru.nobird.android.core.model.Identifiable

data class ReviewInstructionData(
    val reviewInstruction: ReviewInstruction,
    val rubrics: List<Rubric>
) : Identifiable<Long> {
    override val id: Long =
        reviewInstruction.id
}
