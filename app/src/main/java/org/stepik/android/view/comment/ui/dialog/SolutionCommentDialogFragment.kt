package org.stepik.android.view.comment.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_comment_solution.*
import kotlinx.android.synthetic.main.dialog_comment_solution.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz_unsupported.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.base.ui.extension.setTintList
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceStepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeLayoutDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_sorting.ui.delegate.SortingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_sql.ui.delegate.SqlStepQuizFormDelegate
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class SolutionCommentDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "SolutionCommentDialogFragment"

        private const val DISCUSSION_ID_EMPTY = -1L
        private const val ARG_DISCUSSION_THREAD = "discussion_thread"

        fun newInstance(step: Step, attempt: Attempt, submission: Submission, discussionThread: DiscussionThread? = null, discussionId: Long = DISCUSSION_ID_EMPTY): DialogFragment =
            SolutionCommentDialogFragment()
                .apply {
                    this.step = step
                    this.discussionId = discussionId
                    this.attempt = attempt
                    this.submission = submission

                    if (discussionThread != null) {
                        arguments?.putParcelable(ARG_DISCUSSION_THREAD, discussionThread)
                    }
                }
    }

    @Inject
    lateinit var screenManager: ScreenManager

    private var step: Step by argument()
    private var attempt: Attempt by argument()
    private var submission: Submission by argument()

    private val discussionThread: DiscussionThread? by lazy { arguments?.getParcelable<DiscussionThread>(ARG_DISCUSSION_THREAD) }
    private var discussionId: Long by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), theme)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
        injectComponent()
    }

    private fun injectComponent() {
        App.component()
            .commentsComponentBuilder()
            .build()
            .inject(this)
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
        centeredToolbar.navigationIcon = AppCompatResources
            .getDrawable(requireContext(), R.drawable.ic_close_dark)
            ?.setTintList(requireContext(), R.attr.colorControlNormal)

        val state =
            StepQuizView.State.AttemptLoaded(
                attempt,
                StepQuizView.SubmissionState.Loaded(submission),
                StepQuizRestrictions(0, 0, DiscountingPolicyType.NoDiscount)
            )

        val stepQuizFormDelegate = getDelegateForStep(step.block?.name, view)
        if (stepQuizFormDelegate != null) {
            stepQuizFormDelegate.setState(state)

            StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks, hasReview = false, onReviewClicked = {})
                .setState(StepQuizFeedbackMapper().mapToStepQuizFeedbackState(step.block?.name, state))

            stepQuizCodeContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = 0 }
            codeStepLayout?.updateLayoutParams { height = ViewGroup.LayoutParams.WRAP_CONTENT }
        } else {
            stepQuizAction.setOnClickListener {
                val discussionThread = this.discussionThread
                if (discussionThread != null) {
                    screenManager.openDiscussionInWeb(context, step, discussionThread, discussionId)
                } else {
                    screenManager.openSubmissionInWeb(context, step.id, submission.id)
                }
            }
            stepQuizAction.setText(R.string.step_quiz_unsupported_solution_action)
            stepQuizAction.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = resources.getDimensionPixelOffset(R.dimen.space_normal)
            }

            stepQuizFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)

            stepQuizFeedbackBlocks.isVisible = false
        }
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

            AppConstants.TYPE_CODE ->
                CodeStepQuizFormDelegate(
                    containerView = view,
                    codeOptions = step.block?.options!!,
                    codeLayoutDelegate =
                        CodeLayoutDelegate(
                            codeContainerView = view,
                            step = step,
                            codeTemplates = emptyMap(),
                            codeQuizInstructionDelegate = CodeQuizInstructionDelegate(view, isCollapseable = true),
                            codeToolbarAdapter = null,
                            onChangeLanguageClicked = {}
                        )
                ) { _, _ -> }

            AppConstants.TYPE_SORTING ->
                SortingStepQuizFormDelegate(view)

            AppConstants.TYPE_MATCHING ->
                MatchingStepQuizFormDelegate(view)

            AppConstants.TYPE_SQL ->
                SqlStepQuizFormDelegate(view) { _, _ -> }

            else ->
                null
        }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }
    }
}