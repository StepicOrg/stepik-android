package org.stepik.android.view.step_quiz.ui.factory

import androidx.fragment.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData

interface StepQuizFragmentFactory {
    fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper, stepQuizLessonData: StepQuizLessonData): Fragment
    fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean
}