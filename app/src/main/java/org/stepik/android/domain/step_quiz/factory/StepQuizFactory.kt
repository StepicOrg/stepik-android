package org.stepik.android.domain.step_quiz.factory

import org.stepic.droid.persistence.model.StepPersistentWrapper

interface StepQuizFactory {
    fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean
}