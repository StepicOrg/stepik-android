package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.base.ui.extension.viewModel
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class StepQuizReviewFragment :
    Fragment(R.layout.fragment_step_quiz_review),
    StepQuizReviewView,
    SubmissionsDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            StepQuizReviewFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var stepPersistentWrapper: StepPersistentWrapper

    private var stepId: Long by argument()

    private lateinit var stepQuizReviewPresenter: StepQuizReviewPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizReviewView.State>

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
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
    }

    override fun onAction(action: StepQuizReviewView.Action.ViewAction) {
    }

    private fun showSubmissions() {
        SubmissionsDialogFragment
            .newInstance(stepPersistentWrapper.step, isSelectionEnabled = true)
            .showIfNotExists(childFragmentManager, SubmissionsDialogFragment.TAG)
    }

    override fun onSubmissionSelected(submission: Submission, attempt: Attempt) {
        stepQuizReviewPresenter.onNewMessage(StepQuizReviewView.Message.ChangeSubmission(submission, attempt))
    }
}