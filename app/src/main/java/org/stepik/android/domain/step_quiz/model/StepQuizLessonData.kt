package org.stepik.android.domain.step_quiz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.DiscountingPolicyType

@Parcelize
data class StepQuizLessonData(
    val lessonTitle: String,
    val stepCount: Int,
    val discountingPolicyType: DiscountingPolicyType
) : Parcelable {
    constructor(lessonData: LessonData) : this(
        lessonTitle = lessonData.lesson.title.orEmpty(),
        stepCount = lessonData.lesson.steps.size,
        discountingPolicyType = lessonData.section?.discountingPolicy ?: DiscountingPolicyType.NoDiscount
    )
}