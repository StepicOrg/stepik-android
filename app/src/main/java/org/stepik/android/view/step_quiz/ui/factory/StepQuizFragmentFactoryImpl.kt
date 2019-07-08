package org.stepik.android.view.step_quiz.ui.factory

import android.support.v4.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_quiz_text.ui.fragment.TextStepQuizFragment
import org.stepik.android.view.step_quiz_choice.ui.fragment.ChoiceStepQuizFragment
import javax.inject.Inject

class StepQuizFragmentFactoryImpl
@Inject
constructor() : StepQuizFragmentFactory {
    override fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
        when (stepPersistentWrapper.step.block?.name) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER ->
                TextStepQuizFragment.newInstance(stepPersistentWrapper, lessonData)
            AppConstants.TYPE_CHOICE ->
                ChoiceStepQuizFragment.newInstance(stepPersistentWrapper)

            else ->
                Fragment()

        }

    override fun isStepCanHaveQuiz(stepPersistentWrapper: StepPersistentWrapper): Boolean =
        stepPersistentWrapper.step.block?.name != AppConstants.TYPE_VIDEO
}