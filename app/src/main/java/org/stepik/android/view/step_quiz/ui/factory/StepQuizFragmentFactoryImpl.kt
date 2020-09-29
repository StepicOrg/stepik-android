package org.stepik.android.view.step_quiz.ui.factory

import androidx.fragment.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.view.step_quiz_choice.ui.fragment.ChoiceStepQuizFragment
import org.stepik.android.view.step_quiz_code.ui.fragment.CodeStepQuizFragment
import org.stepik.android.view.step_quiz_fill_blanks.ui.fragment.FillBlanksStepQuizFragment
import org.stepik.android.view.step_quiz_matching.ui.fragment.MatchingStepQuizFragment
import org.stepik.android.view.step_quiz_sorting.ui.fragment.SortingStepQuizFragment
import org.stepik.android.view.step_quiz_sql.ui.fragment.SqlStepQuizFragment
import org.stepik.android.view.step_quiz_text.ui.fragment.TextStepQuizFragment
import org.stepik.android.view.step_quiz_unsupported.ui.fragment.UnsupportedStepQuizFragment
import org.stepik.android.view.step_quiz_pycharm.ui.fragment.PyCharmStepQuizFragment
import org.stepik.android.view.step_quiz_review.ui.fragment.StepQuizReviewFragment
import javax.inject.Inject

class StepQuizFragmentFactoryImpl
@Inject
constructor() : StepQuizFragmentFactory {
    override fun createStepQuizFragment(stepPersistentWrapper: StepPersistentWrapper): Fragment =
        when (stepPersistentWrapper.step.block?.name) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER -> {
                val instructionType = stepPersistentWrapper.step.instructionType
                if (stepPersistentWrapper.step.actions?.doReview != null && instructionType != null) {
                    StepQuizReviewFragment.newInstance(stepPersistentWrapper.step.id, instructionType)
                } else {
                    TextStepQuizFragment.newInstance(stepPersistentWrapper.step.id)
                }
            }

            AppConstants.TYPE_CHOICE ->
                ChoiceStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            AppConstants.TYPE_CODE ->
                CodeStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            AppConstants.TYPE_SORTING ->
                SortingStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            AppConstants.TYPE_MATCHING ->
                MatchingStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            AppConstants.TYPE_PYCHARM ->
                PyCharmStepQuizFragment.newInstance()

            AppConstants.TYPE_SQL ->
                SqlStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            AppConstants.TYPE_FILL_BLANKS ->
                FillBlanksStepQuizFragment.newInstance(stepPersistentWrapper.step.id)

            else ->
                UnsupportedStepQuizFragment.newInstance(stepPersistentWrapper.step.id)
        }
}