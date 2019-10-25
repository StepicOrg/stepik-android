package org.stepik.android.view.comment.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_comment_solution.*
import kotlinx.android.synthetic.main.dialog_comment_solution.view.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceStepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_sorting.ui.delegate.SortingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_sql.ui.delegate.SqlStepQuizFormDelegate
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import ru.nobird.android.view.base.ui.extension.argument

class SolutionCommentDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "SolutionCommentDialogFragment"

        fun newInstance(step: Step, attempt: Attempt, submission: Submission): DialogFragment =
            SolutionCommentDialogFragment()
                .apply {
                    this.step = step
                    this.attempt = attempt
                    this.submission = submission
                }
    }

    private var step: Step by argument()
    private var attempt: Attempt by argument()
    private var submission: Submission by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), theme)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_comment_solution, container, false)
            .also {
                it.solutionContainer.addView(inflater.inflate(getLayoutResForStep(step.block?.name), it.solutionContainer, false), 1)
            }

    @LayoutRes
    private fun getLayoutResForStep(blockName: String?): Int =
        when (blockName) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER ->
                R.layout.layout_step_quiz_text

            AppConstants.TYPE_CHOICE ->
                R.layout.layout_step_quiz_choice

            AppConstants.TYPE_CODE ->
                R.layout.layout_step_quiz_code

            AppConstants.TYPE_SORTING,
            AppConstants.TYPE_MATCHING ->
                R.layout.layout_step_quiz_sorting

            AppConstants.TYPE_SQL ->
                R.layout.layout_step_quiz_sql

            else ->
                R.layout.fragment_step_quiz_unsupported
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbarTitle.text = getString(R.string.comment_solution_pattern, submission.id)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)

        val state =
            StepQuizView.State.AttemptLoaded(
                attempt,
                StepQuizView.SubmissionState.Loaded(submission),
                StepQuizRestrictions(0, 0, DiscountingPolicyType.NoDiscount)
            )
        getDelegateForStep(step.block?.name, view)
            ?.setState(state)

        StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks, hasReview = false, onReviewClicked = {})
            .setState(StepQuizFeedbackMapper().mapToStepQuizFeedbackState(step.block?.name, state))
    }

    private fun getDelegateForStep(blockName: String?, view: View): StepQuizFormDelegate? =
        when (blockName) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER ->
                TextStepQuizFormDelegate(view, blockName)

            AppConstants.TYPE_CHOICE ->
                ChoiceStepQuizFormDelegate(view)

//            AppConstants.TYPE_CODE ->
//                CodeStepQuizFormDelegate(view, step.block?.options!!, CodeLayoutDelegate(view.codeStepLayout, step, )) { l, c -> }

            AppConstants.TYPE_SORTING ->
                SortingStepQuizFormDelegate(view)

            AppConstants.TYPE_MATCHING ->
                MatchingStepQuizFormDelegate(view)

            AppConstants.TYPE_SQL ->
                SqlStepQuizFormDelegate(view) { l, c -> }

            else ->
                null
        }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
            }
    }
}