package org.stepik.android.view.step_quiz_review.ui.factory

import android.view.View
import androidx.fragment.app.FragmentManager
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.delegate.FillBlanksStepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_sorting.ui.delegate.SortingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_table.ui.delegate.TableStepQuizFormDelegate
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate

class StepQuizFormReviewFactory(
    private val fragmentManager: FragmentManager,
    private val onQuizChanged: (ReplyResult) -> Unit
) : StepQuizFormFactory {
    override fun getLayoutResForStep(blockName: String?): Int =
        when (blockName) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER ->
                R.layout.layout_step_quiz_text

            AppConstants.TYPE_CHOICE ->
                R.layout.layout_step_quiz_choice

            AppConstants.TYPE_SORTING,
            AppConstants.TYPE_MATCHING ->
                R.layout.layout_step_quiz_sorting

            AppConstants.TYPE_FILL_BLANKS ->
                R.layout.layout_step_quiz_fill_blanks

            AppConstants.TYPE_TABLE ->
                R.layout.layout_step_quiz_table

            else ->
                R.layout.fragment_step_quiz_unsupported
        }

    override fun getDelegateForStep(blockName: String?, view: View): StepQuizFormDelegate? =
        when (blockName) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER ->
                TextStepQuizFormDelegate(view, blockName, onQuizChanged)

            AppConstants.TYPE_CHOICE ->
                ChoiceStepQuizFormDelegate(view, onQuizChanged)

            AppConstants.TYPE_SORTING ->
                SortingStepQuizFormDelegate(view, onQuizChanged)

            AppConstants.TYPE_MATCHING ->
                MatchingStepQuizFormDelegate(view, onQuizChanged)

            AppConstants.TYPE_FILL_BLANKS ->
                FillBlanksStepQuizFormDelegate(view, fragmentManager, onQuizChanged)

            AppConstants.TYPE_TABLE ->
                TableStepQuizFormDelegate(view, fragmentManager, onQuizChanged)

            else ->
                null
        }
}