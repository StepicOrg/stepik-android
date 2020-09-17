package org.stepik.android.domain.step_quiz.factory

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import javax.inject.Inject

class StepQuizFactoryImpl
@Inject
constructor() : StepQuizFactory {
    override fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean =
        stepPersistentWrapper.step.block?.name?.let { name ->
            name != AppConstants.TYPE_VIDEO &&
                    name != AppConstants.TYPE_TEXT
        } ?: false
}