package org.stepik.android.presentation.step

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection

interface StepView {
    sealed class State {
        object Idle : State()
        class Loaded(
            val stepWrapper: StepPersistentWrapper,
            val lessonData: LessonData
        ) : State()
    }

    fun setNavigation(directions: Set<StepNavigationDirection>)
    fun showLesson(direction: StepNavigationDirection, lessonData: LessonData)
}