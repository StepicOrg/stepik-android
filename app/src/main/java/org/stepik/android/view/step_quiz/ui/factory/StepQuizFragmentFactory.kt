package org.stepik.android.view.step_quiz.ui.factory

import androidx.core.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData

interface StepQuizFragmentFactory {
    fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment
    fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean
}