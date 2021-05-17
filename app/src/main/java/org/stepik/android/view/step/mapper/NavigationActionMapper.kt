package org.stepik.android.view.step.mapper

import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Course
import org.stepik.android.view.step.model.NavigationAction
import javax.inject.Inject

class NavigationActionMapper
@Inject
constructor() {
    fun mapToCoursePurchaseAction(course: Course?): NavigationAction =
        if (course == null) {
            NavigationAction.Unknown
        } else {
            NavigationAction.ShowLessonDemoComplete(course)
        }

    fun mapToShowLessonAction(
        direction: StepNavigationDirection,
        lessonData: LessonData,
        isAutoplayEnabled: Boolean = false
    ): NavigationAction =
        NavigationAction.ShowLesson(direction, lessonData, isAutoplayEnabled)
}