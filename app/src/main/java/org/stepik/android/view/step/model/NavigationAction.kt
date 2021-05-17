package org.stepik.android.view.step.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Course

sealed class NavigationAction : Parcelable {
    @Parcelize
    data class ShowLesson(
        val direction: StepNavigationDirection,
        val lessonData: LessonData,
        val isAutoplayEnabled: Boolean = false
    ) : NavigationAction()

    @Parcelize
    data class ShowLessonDemoComplete(val course: Course) : NavigationAction()

    @Parcelize
    object Unknown : NavigationAction()
}
