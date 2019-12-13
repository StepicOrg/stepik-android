package org.stepik.android.view.step_content.ui.factory

import androidx.fragment.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData

interface StepContentFragmentFactory {
    fun createStepContentFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment
}