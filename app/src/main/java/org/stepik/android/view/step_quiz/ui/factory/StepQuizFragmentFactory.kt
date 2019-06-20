package org.stepik.android.view.step_quiz.ui.factory

import android.support.v4.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper

interface StepQuizFragmentFactory {
    fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment
    fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean
}