package org.stepik.android.view.step_content.ui.factory

import androidx.fragment.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_content_text.ui.fragment.TextStepContentFragment
import org.stepik.android.view.step_content_video.ui.fragment.VideoStepContentFragment
import javax.inject.Inject

class StepContentFragmentFactoryImpl
@Inject
constructor() : StepContentFragmentFactory {
    override fun createStepContentFragment(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
        when (stepPersistentWrapper.step.block?.name) {
            AppConstants.TYPE_VIDEO ->
                VideoStepContentFragment.newInstance(stepPersistentWrapper, lessonData.lesson.title.orEmpty())

            else ->
                TextStepContentFragment.newInstance(stepPersistentWrapper, lessonData.lesson.title.orEmpty(), lessonData.lesson.actions?.editLesson != null)
        }
}