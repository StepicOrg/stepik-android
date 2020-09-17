package org.stepik.android.view.step_quiz.ui.factory

import androidx.fragment.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper

interface StepQuizFragmentFactory {
    fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment
}