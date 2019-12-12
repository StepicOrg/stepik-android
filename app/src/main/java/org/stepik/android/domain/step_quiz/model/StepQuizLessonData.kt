package org.stepik.android.domain.step_quiz.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.DiscountingPolicyType

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lessonTitle)
        parcel.writeInt(stepCount)
        parcel.writeInt(discountingPolicyType.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StepQuizLessonData> {
        override fun createFromParcel(parcel: Parcel): StepQuizLessonData =
            StepQuizLessonData(
                parcel.readString()!!,
                parcel.readInt(),
                DiscountingPolicyType.values()[parcel.readInt()]
            )

        override fun newArray(size: Int): Array<StepQuizLessonData?> =
            arrayOfNulls(size)
    }
}