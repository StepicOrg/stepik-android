package org.stepik.android.view.step_quiz.ui.factory

import android.support.v4.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import javax.inject.Inject

class StepQuizFragmentFactoryImpl
@Inject
constructor() : StepQuizFragmentFactory {
    override fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment =
        Fragment()

    override fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean =
        stepPersistentWrapper.step.block?.name != AppConstants.TYPE_VIDEO
}