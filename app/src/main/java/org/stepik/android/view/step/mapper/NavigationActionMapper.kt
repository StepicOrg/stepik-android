package org.stepik.android.view.step.mapper

import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Course
import org.stepik.android.view.step.model.StepNavigationAction
import javax.inject.Inject

class NavigationActionMapper
@Inject
constructor() {
    fun mapToCoursePurchaseAction(course: Course?): StepNavigationAction =
        if (course == null) {
            StepNavigationAction.Unknown
        } else {
            StepNavigationAction.ShowLessonDemoComplete(course)
        }

    fun mapToShowLessonAction(
        direction: StepNavigationDirection,
        lessonData: LessonData,
        isAutoplayEnabled: Boolean = false
    ): StepNavigationAction =
        StepNavigationAction.ShowLesson(direction, lessonData, isAutoplayEnabled)
}