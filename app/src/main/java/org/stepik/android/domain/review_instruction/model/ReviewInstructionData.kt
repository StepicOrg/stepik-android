package org.stepik.android.domain.review_instruction.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.rubric.model.Rubric
import ru.nobird.app.core.model.Identifiable

@Parcelize
data class ReviewInstructionData(
    val reviewInstruction: ReviewInstruction,
    val rubrics: List<Rubric>
) : Identifiable<Long>, Parcelable {
    override val id: Long =
        reviewInstruction.id

    val maxScore: Int =
        rubrics.sumBy { it.cost }
}
