package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz_review.*
import kotlinx.android.synthetic.main.fragment_step_quiz_review_peer.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_header.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_header.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.analytic.reportStepEvent
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewFeature
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewViewModel
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz_review.routing.StepQuizReviewDeepLinkBuilder
import org.stepik.android.view.step_quiz_review.ui.delegate.StepQuizReviewDelegate
import org.stepik.android.view.step_quiz_review.ui.factory.StepQuizFormReviewFactory
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class StepQuizReviewFragment :
    Fragment(),
    ReduxView<StepQuizReviewFeature.State, StepQuizReviewFeature.Action.ViewAction>,
    SubmissionsDialogFragment.Callback {
    companion object {
        val supportedQuizTypes =
            setOf(
                AppConstants.TYPE_STRING,
                AppConstants.TYPE_NUMBER,
                AppConstants.TYPE_MATH,
                AppConstants.TYPE_FREE_ANSWER,
                AppConstants.TYPE_CHOICE,
                AppConstants.TYPE_MATCHING,
                AppConstants.TYPE_SORTING,
                AppConstants.TYPE_FILL_BLANKS,
                AppConstants.TYPE_TABLE
            )

        fun newInstance(stepId: Long, instructionType: ReviewStrategyType): Fragment =
            StepQuizReviewFragment()
                .apply {
                    this.stepId = stepId
                    this.instructionType = instructionType
                }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var stepQuizReviewDeepLinkBuilder: StepQuizReviewDeepLinkBuilder

    @Inject
    internal lateinit var stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>

    @Inject
    internal lateinit var lessonData: LessonData

    private var stepId: Long by argument()
    private var instructionType: ReviewStrategyType by argument()

    private lateinit var stepWrapper: StepPersistentWrapper

    private val stepQuizReviewViewModel: StepQuizReviewViewModel by reduxViewModel(this) { viewModelFactory }
    private lateinit var delegate: StepQuizReviewDelegate

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizReviewFeature.State>

    private lateinit var quizView: View

    private lateinit var stepQuizFormFactory: StepQuizFormFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()
        stepWrapper = stepWrapperRxRelay.value ?: throw IllegalStateException("Step wrapper cannot be null")
        stepQuizFormFactory = StepQuizFormReviewFactory(childFragmentManager, ::syncReplyState)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_step_quiz_review, container, false) as ViewGroup

        @LayoutRes
        val layoutId =
            when (instructionType) {
                ReviewStrategyType.PEER ->
                    R.layout.fragment_step_quiz_review_peer

                ReviewStrategyType.INSTRUCTOR ->
                    R.layout.fragment_step_quiz_review_instructor
            }

        // we don't pass [root] in order to clear margins
        quizView = inflater.inflate(stepQuizFormFactory.getLayoutResForStep(stepWrapper.step.block?.name), null)

        inflater.inflate(layoutId, view)
            .also {
                it.reviewStep1Container.addView(quizView)
            }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizReviewFeature.State.Idle>(stepQuizReviewLoading)
        viewStateDelegate.addState<StepQuizReviewFeature.State.Loading>(stepQuizReviewLoading)
        viewStateDelegate.addState<StepQuizReviewFeature.State.Error>(stepQuizReviewNetworkError)
        viewStateDelegate.addState<StepQuizReviewFeature.State.SubmissionNotMade>(stepQuizReviewContainer)
        viewStateDelegate.addState<StepQuizReviewFeature.State.SubmissionNotSelected>(stepQuizReviewContainer)
        viewStateDelegate.addState<StepQuizReviewFeature.State.SubmissionSelected>(stepQuizReviewContainer)
        viewStateDelegate.addState<StepQuizReviewFeature.State.Completed>(stepQuizReviewContainer)

        stepQuizReviewNetworkError.tryAgain
            .setOnClickListener { stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.InitWithStep(stepWrapper, lessonData, forceUpdate = true)) }

        val actionListener = object : StepQuizReviewDelegate.ActionListener {
            override fun onSelectDifferentSubmissionClicked() {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_SELECT_DIFFERENT_SUBMISSION, stepWrapper.step)
                showSubmissions()
            }

            override fun onCreateSessionClicked() {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_SEND_CURRENT_SUBMISSION, stepWrapper.step)
                stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.CreateSessionWithCurrentSubmission)
            }

            override fun onSolveAgainClicked() {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_SOLVE_AGAIN, stepWrapper.step)
                stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.SolveAgain(stepWrapper.step))
            }

            override fun onQuizTryAgainClicked() {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_QUIZ_TRY_AGAIN, stepWrapper.step)
                stepQuizReviewViewModel.onNewMessage(
                    StepQuizReviewFeature.Message.StepQuizMessage(StepQuizFeature.Message.InitWithStep(stepWrapper, lessonData, forceUpdate = true))
                )
            }

            override fun onStartReviewClicked() {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_START_REVIEW, stepWrapper.step)
                stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.StartReviewWithCurrentSession)
            }

            override fun onTakenReviewClicked(sessionId: Long) {
                analytic.reportStepEvent(AmplitudeAnalytic.PeerReview.REVIEW_VIEW_REVIEW, stepWrapper.step)
                openInWeb(
                    R.string.step_quiz_review_taken_title,
                    stepQuizReviewDeepLinkBuilder.createTakenReviewDeepLink(sessionId)
                )
            }
        }

        val blockName = stepWrapper.step.block?.name
        val stepQuizBlockDelegate = StepQuizFeedbackBlocksDelegate(quizFeedbackView, isTeacher = false, hasReview = false) {}
        val quizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = stepQuizFormFactory.getDelegateForStep(blockName, view) ?: throw IllegalStateException("Unsupported quiz"),
                stepQuizFeedbackBlocksDelegate = stepQuizBlockDelegate,

                stepQuizActionButton = reviewStep1ActionButton,
                stepRetryButton = reviewStep1ActionRetry,

                stepQuizDiscountingPolicy = reviewStep1Discounting,
                stepQuizReviewTeacherMessage = null,
                onNewMessage = { stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.StepQuizMessage(it)) }
            )

        delegate =
            StepQuizReviewDelegate(
                view, instructionType, actionListener,
                blockName,
                quizView,
                quizDelegate,
                stepQuizBlockDelegate
            )
    }

    override fun render(state: StepQuizReviewFeature.State) {
        viewStateDelegate.switchState(state)
        delegate.render(state)
    }

    override fun onAction(action: StepQuizReviewFeature.Action.ViewAction) {
        when (action) {
            is StepQuizReviewFeature.Action.ViewAction.ShowNetworkError ->
                view?.snackbar(messageRes = R.string.connectionProblems)

            is StepQuizReviewFeature.Action.ViewAction.OpenReviewScreen ->
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
            .newInstance(stepWrapper.step, status = Submission.Status.CORRECT, isSelectionEnabled = true)
            .showIfNotExists(childFragmentManager, SubmissionsDialogFragment.TAG)
    }

    override fun onSubmissionSelected(submission: Submission, attempt: Attempt) {
        stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.ChangeSubmission(submission, attempt))
    }

    private fun syncReplyState(replyResult: ReplyResult) {
        stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.StepQuizMessage(StepQuizFeature.Message.SyncReply(replyResult.reply)))
    }
}