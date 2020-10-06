package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.layout_step_quiz_review_header.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.base.ui.extension.viewModel
import org.stepik.android.view.in_app_web_view.InAppWebViewDialogFragment
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.delegate.FillBlanksStepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_review.routing.StepQuizReviewDeepLinkBuilder
import org.stepik.android.view.step_quiz_review.ui.delegate.StepQuizReviewDelegate
import org.stepik.android.view.step_quiz_sorting.ui.delegate.SortingStepQuizFormDelegate
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class StepQuizReviewFragment :
    Fragment(),
    StepQuizReviewView,
    SubmissionsDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long, instructionType: ReviewStrategyType): Fragment =
            StepQuizReviewFragment()
                .apply {
                    this.stepId = stepId
                    this.instructionType = instructionType
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var stepQuizReviewDeepLinkBuilder: StepQuizReviewDeepLinkBuilder

    @Inject
    internal lateinit var stepPersistentWrapper: StepPersistentWrapper

    private var stepId: Long by argument()

    private var instructionType: ReviewStrategyType by argument()

    private lateinit var stepQuizReviewPresenter: StepQuizReviewPresenter
    private lateinit var delegate: StepQuizReviewDelegate

    private lateinit var quizView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        stepQuizReviewPresenter = viewModel(viewModelFactory)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        @LayoutRes
        val layoutId =
            when (instructionType) {
                ReviewStrategyType.PEER ->
                    R.layout.fragment_step_quiz_review_peer

                ReviewStrategyType.INSTRUCTOR ->
                    R.layout.fragment_step_quiz_review_instructor
            }

        // we don't pass [root] in order to clear margins
        quizView = inflater.inflate(getLayoutResForStep(stepPersistentWrapper.step.block?.name), null)

        return inflater.inflate(layoutId, container, false)
            .also {
                it.reviewStep1Container.addView(quizView)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionListener = object : StepQuizReviewDelegate.ActionListener {
            override fun onSelectDifferentSubmissionClicked() {
                showSubmissions()
            }

            override fun onCreateSessionClicked() {
                stepQuizReviewPresenter.onNewMessage(StepQuizReviewView.Message.CreateSessionWithCurrentSubmission)
            }

            override fun onSolveAgainClicked() {
                stepQuizReviewPresenter.onNewMessage(StepQuizReviewView.Message.SolveAgain)
            }

            override fun onStartReviewClicked() {
                stepQuizReviewPresenter.onNewMessage(StepQuizReviewView.Message.StartReviewWithCurrentSession)
            }

            override fun onTakenReviewClicked(sessionId: Long) {
                openInWeb(
                    R.string.step_quiz_review_taken_title,
                    stepQuizReviewDeepLinkBuilder.createTakenReviewDeepLink(sessionId)
                )
            }
        }

        delegate =
            StepQuizReviewDelegate(view, instructionType, actionListener, quizView, getDelegateForStep(stepPersistentWrapper.step.block?.name, view)!!)
    }

    // todo reduce duplication from SolutionCommentDialogFragment
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

            AppConstants.TYPE_SORTING,
            AppConstants.TYPE_MATCHING ->
                R.layout.layout_step_quiz_sorting

            AppConstants.TYPE_SQL ->
                R.layout.layout_step_quiz_sql

            AppConstants.TYPE_FILL_BLANKS ->
                R.layout.layout_step_quiz_fill_blanks

            else ->
                R.layout.fragment_step_quiz_unsupported
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

            AppConstants.TYPE_SORTING ->
                SortingStepQuizFormDelegate(view)

            AppConstants.TYPE_MATCHING ->
                MatchingStepQuizFormDelegate(view)

            AppConstants.TYPE_FILL_BLANKS ->
                FillBlanksStepQuizFormDelegate(view, childFragmentManager)

            else ->
                null
        }

    override fun onStart() {
        super.onStart()
        stepQuizReviewPresenter.attachView(this)
    }

    override fun onStop() {
        stepQuizReviewPresenter.detachView(this)
        super.onStop()
    }

    override fun render(state: StepQuizReviewView.State) {
        delegate.render(state)
    }

    override fun onAction(action: StepQuizReviewView.Action.ViewAction) {
        when (action) {
            is StepQuizReviewView.Action.ViewAction.ShowNetworkError ->
                view?.snackbar(messageRes = R.string.connectionProblems)

            is StepQuizReviewView.Action.ViewAction.OpenReviewScreen ->
                openInWeb(R.string.step_quiz_review_given_title, stepQuizReviewDeepLinkBuilder.createReviewDeepLink(action.reviewId))
        }
    }

    private fun openInWeb(@StringRes titleRes: Int, url: String) {
        InAppWebViewDialogFragment
            .newInstance(getString(titleRes), url, isProvideAuth = true)
            .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
    }

    /**
     * Submission selection
     */
    private fun showSubmissions() {
        SubmissionsDialogFragment
            .newInstance(stepPersistentWrapper.step, isSelectionEnabled = true)
            .showIfNotExists(childFragmentManager, SubmissionsDialogFragment.TAG)
    }

    override fun onSubmissionSelected(submission: Submission, attempt: Attempt) {
        stepQuizReviewPresenter.onNewMessage(StepQuizReviewView.Message.ChangeSubmission(submission, attempt))
    }
}