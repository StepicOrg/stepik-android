package org.stepik.android.view.step_quiz.ui.factory

import android.support.v4.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.view.step_quiz_string.ui.fragment.StringStepQuizFragment
import javax.inject.Inject

class StepQuizFragmentFactoryImpl
@Inject
constructor() : StepQuizFragmentFactory {
    override fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment =
        when (stepPersistentWrapper.step.block?.name) {
            AppConstants.TYPE_STRING ->
                StringStepQuizFragment.newInstance(stepPersistentWrapper)

            else ->
                Fragment()
        }

    override fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean =
        stepPersistentWrapper.step.block?.name != AppConstants.TYPE_VIDEO
}