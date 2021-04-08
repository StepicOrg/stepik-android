package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz.*
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz_review_teacher.*
import kotlinx.android.synthetic.main.fragment_step_quiz_review_teacher.view.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherViewModel
import org.stepik.android.view.lesson.ui.interfaces.NextMoveable
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizViewStateDelegateFactory
import org.stepik.android.view.step_quiz_review.ui.factory.StepQuizFormReviewFactory
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.toPx
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class StepQuizReviewTeacherFragment :
    Fragment(),
    ReduxView<StepQuizReviewTeacherFeature.State, StepQuizReviewTeacherFeature.Action.ViewAction> {
    companion object {
        val supportedQuizTypes = StepQuizReviewFragment.supportedQuizTypes

        fun newInstance(stepId: Long, instructionType: ReviewStrategyType): Fragment =
            StepQuizReviewTeacherFragment()
                .apply {
                    this.stepId = stepId
                    this.instructionType = instructionType
                }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var stepQuizViewStateDelegateFactory: StepQuizViewStateDelegateFactory

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>

    @Inject
    internal lateinit var lessonData: LessonData

    private val stepQuizReviewTeacherViewModel: StepQuizReviewTeacherViewModel by reduxViewModel(this) { viewModelFactory }

    private var stepId: Long by argument()
    private var instructionType: ReviewStrategyType by argument()

    private lateinit var stepWrapper: StepPersistentWrapper

    private lateinit var stepQuizFormFactory: StepQuizFormFactory

    private lateinit var quizLayout: View
    private lateinit var quizDelegate: StepQuizDelegate

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizReviewTeacherFeature.State>
    private lateinit var quizViewStateDelegate: ViewStateDelegate<StepQuizFeature.State>

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
        val view = inflater.inflate(R.layout.fragment_step_quiz_review_teacher, container, false)
        val quizContainer = view.stepQuizReviewTeacherQuiz as ConstraintLayout
        val quizLayoutRes = stepQuizFormFactory.getLayoutResForStep(stepWrapper.step.block?.name)
        quizLayout = inflater.inflate(quizLayoutRes, quizContainer, false)
        quizContainer.addView(quizLayout)
        realignQuizLayout(quizContainer, quizLayout)
        return view
    }

    /**
     * Align quiz container as vertical linear layout for smooth collapsing animation
     */
    private fun realignQuizLayout(quizContainer: ConstraintLayout, quizLayout: View) {
        val feedbackBlocks = quizContainer.stepQuizFeedbackBlocks

        quizLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToTop = ConstraintLayout.LayoutParams.UNSET
        }
        feedbackBlocks.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToTop = ConstraintLayout.LayoutParams.UNSET
            topToBottom = quizLayout.id
            topMargin = 16.toPx()
        }
        quizContainer.stepQuizActionContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = feedbackBlocks.id
            topMargin = 16.toPx()
            bottomMargin = 16.toPx()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Idle>(
            stepQuizReviewTeacherQuizSkeleton,
            stepQuizReviewTeacherButtonSkeleton
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Loading>(
            stepQuizReviewTeacherQuizSkeleton,
            stepQuizReviewTeacherButtonSkeleton
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Error>(
            stepQuizReviewTeacherNetworkError
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Data>(
            stepQuizReviewTeacherSpoiler,
            stepQuizReviewTeacherContainer,
            stepQuizReviewTeacherDescription,
            stepQuizReviewTeacherSubmissions
        )

        quizViewStateDelegate = stepQuizViewStateDelegateFactory
            .create(stepQuizReviewTeacherQuiz, quizLayout)

        val blockName = stepWrapper.step.block?.name

        stepQuizReviewTeacherSpoiler.setOnClickListener {
            stepQuizReviewTeacherArrow.changeState()
            if (stepQuizReviewTeacherArrow.isExpanded()) {
                stepQuizReviewTeacherContainer.expand()
            } else {
                stepQuizReviewTeacherContainer.collapse()
            }
        }

        stepQuizReviewTeacherMessage.isVisible = false

        val stepQuizBlockDelegate =
            StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks, isTeacher = false, hasReview = false) {}

        quizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = stepQuizFormFactory.getDelegateForStep(blockName, view) ?: throw IllegalStateException("Unsupported quiz"),
                stepQuizFeedbackBlocksDelegate = stepQuizBlockDelegate,

                stepQuizActionButton = stepQuizAction,
                stepRetryButton = stepQuizRetry,

                stepQuizDiscountingPolicy = stepQuizDiscountingPolicy,
                stepQuizReviewTeacherMessage = null,
                onNewMessage = {
                    stepQuizReviewTeacherViewModel.onNewMessage(StepQuizReviewTeacherFeature.Message.StepQuizMessage(it))
                },
                onNextClicked = {
                    (parentFragment as? NextMoveable)?.moveNext()
                }
            )

        stepQuizReviewTeacherNetworkError.tryAgain.setOnClickListener {
            stepQuizReviewTeacherViewModel
                .onNewMessage(StepQuizReviewTeacherFeature.Message.InitWithStep(stepWrapper, lessonData, instructionType, forceUpdate = true))
        }
    }

    override fun render(state: StepQuizReviewTeacherFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizReviewTeacherFeature.State.Data) {
            stepQuizReviewTeacherContainer.isVisible =
                stepQuizReviewTeacherArrow.isExpanded()

            quizViewStateDelegate.switchState(state.quizState)
            stepQuizReviewTeacherMessage.isVisible = false
            if (state.quizState is StepQuizFeature.State.AttemptLoaded) {
                quizDelegate.setState(state.quizState)
            }

            stepQuizReviewTeacherDescription.text =
                when (state.instructionType) {
                    ReviewStrategyType.INSTRUCTOR ->
                        if (state.availableReviewCount > 0) {
                            val submissions = resources.getQuantityString(R.plurals.solutions, state.availableReviewCount, state.availableReviewCount)

                            HtmlCompat.fromHtml(getString(R.string.step_quiz_review_teacher_notice_instructors_submissions, submissions), HtmlCompat.FROM_HTML_MODE_COMPACT)
                        } else {
                            getString(R.string.step_quiz_review_teacher_notice_instructors_no_submissions)
                        }

                    ReviewStrategyType.PEER ->
                        getString(R.string.step_quiz_review_teacher_notice_peer)
                }
        }
    }

    override fun onAction(action: StepQuizReviewTeacherFeature.Action.ViewAction) {
        // no op
    }

    private fun syncReplyState(replyResult: ReplyResult) {
        stepQuizReviewTeacherViewModel.onNewMessage(
            StepQuizReviewTeacherFeature.Message.StepQuizMessage(
                StepQuizFeature.Message.SyncReply(replyResult.reply)))
    }
}