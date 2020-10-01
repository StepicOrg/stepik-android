package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.base.ui.extension.viewModel
import org.stepik.android.view.in_app_web_view.InAppWebViewDialogFragment
import org.stepik.android.view.step_quiz_review.routing.StepQuizReviewDeepLinkBuilder
import org.stepik.android.view.step_quiz_review.ui.delegate.StepQuizReviewDelegate
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

        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionListener = object : StepQuizReviewDelegate.ActionListener {
            override fun onTakenReviewClicked(sessionId: Long) {
                openInWeb(
                    R.string.step_quiz_review_taken_title,
                    stepQuizReviewDeepLinkBuilder.createTakenReviewDeepLink(sessionId)
                )
            }
        }
        delegate = StepQuizReviewDelegate(view, instructionType, actionListener)
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
        // todo
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