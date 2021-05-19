package org.stepik.android.view.step.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Course

sealed class StepNavigationAction : Parcelable {
    @Parcelize
    data class ShowLesson(
        val direction: StepNavigationDirection,
        val lessonData: LessonData,
        val isAutoplayEnabled: Boolean = false
    ) : StepNavigationAction()

    @Parcelize
    data class ShowLessonDemoComplete(val course: Course) : StepNavigationAction()

    @Parcelize
    data class ShowSectionUnavailable(val sectionUnavailableAction: SectionUnavailableAction) : StepNavigationAction()

    @Parcelize
    object Unknown : StepNavigationAction()
}
