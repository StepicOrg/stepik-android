package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.jakewharton.rxrelay2.BehaviorRelay
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.databinding.FragmentStepQuizReviewTeacherBinding
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherViewModel
import org.stepik.android.view.lesson.ui.interfaces.Moveable
import org.stepik.android.view.step.ui.interfaces.StepMenuNavigator
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizViewStateDelegateFactory
import org.stepik.android.view.step_quiz_review.ui.factory.StepQuizFormReviewFactory
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.safeCast
import ru.nobird.app.presentation.redux.container.ReduxView
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

    private val binding: FragmentStepQuizReviewTeacherBinding by viewBinding(FragmentStepQuizReviewTeacherBinding::bind)

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
        val quizContainer = view.findViewById<ConstraintLayout>(R.id.stepQuizReviewTeacherQuiz)
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
        val feedbackBlocks = quizContainer.findViewById<View>(R.id.stepQuizFeedbackBlocks)

        quizLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToTop = ConstraintLayout.LayoutParams.UNSET
        }
        feedbackBlocks.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToTop = ConstraintLayout.LayoutParams.UNSET
            topToBottom = quizLayout.id
            topMargin = 16.toPx()
        }
        quizContainer.findViewById<View>(R.id.stepQuizActionContainer).updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = feedbackBlocks.id
            topMargin = 16.toPx()
            bottomMargin = 16.toPx()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Idle>(
            binding.stepQuizReviewTeacherQuizSkeleton,
            binding.stepQuizReviewTeacherButtonSkeleton
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Loading>(
            binding.stepQuizReviewTeacherQuizSkeleton,
            binding.stepQuizReviewTeacherButtonSkeleton
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Error>(
            binding.stepQuizReviewTeacherNetworkError.root
        )
        viewStateDelegate.addState<StepQuizReviewTeacherFeature.State.Data>(
            binding.stepQuizReviewTeacherSpoiler,
            binding.stepQuizReviewTeacherContainer,
            binding.stepQuizReviewTeacherDescription,
            binding.stepQuizReviewTeacherSubmissions
        )

        val stepQuizView = binding.stepQuizReviewTeacherQuiz
        quizViewStateDelegate = stepQuizViewStateDelegateFactory
            .create(stepQuizView, quizLayout)

        val blockName = stepWrapper.step.block?.name

        binding.stepQuizReviewTeacherSpoiler.setOnClickListener {
            binding.stepQuizReviewTeacherArrow.changeState()
            if (binding.stepQuizReviewTeacherArrow.isExpanded()) {
                binding.stepQuizReviewTeacherContainer.expand()
            } else {
                binding.stepQuizReviewTeacherContainer.collapse()
            }
        }

        val stepQuizReviewTeacherMessage = stepQuizView.findViewById<View>(R.id.stepQuizReviewTeacherMessage)
        stepQuizReviewTeacherMessage.isVisible = false

        val stepQuizFeedbackBlocks = stepQuizView.findViewById<View>(R.id.stepQuizFeedbackBlocks)
        val stepQuizBlockDelegate =
            StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks, isTeacher = false, hasReview = false) {}

        val stepQuizActionContainer = stepQuizView.findViewById<View>(R.id.stepQuizActionContainer)
        val stepQuizDiscountingPolicy = stepQuizView.findViewById<TextView>(R.id.stepQuizDiscountingPolicy)

        quizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = stepQuizFormFactory.getDelegateForStep(blockName, view) ?: throw IllegalStateException("Unsupported quiz"),
                stepQuizFeedbackBlocksDelegate = stepQuizBlockDelegate,

                stepQuizActionButton = stepQuizActionContainer.findViewById(R.id.stepQuizAction),
                stepRetryButton = stepQuizActionContainer.findViewById(R.id.stepQuizRetry),

                stepQuizDiscountingPolicy = stepQuizDiscountingPolicy,
                stepQuizReviewTeacherMessage = null,
                onNewMessage = {
                    stepQuizReviewTeacherViewModel.onNewMessage(StepQuizReviewTeacherFeature.Message.StepQuizMessage(it))
                },
                onNextClicked = {
                    (parentFragment as? Moveable)?.move()
                }
            )

        binding.stepQuizReviewTeacherNetworkError.tryAgain.setOnClickListener {
            stepQuizReviewTeacherViewModel
                .onNewMessage(StepQuizReviewTeacherFeature.Message.InitWithStep(stepWrapper, lessonData, instructionType, forceUpdate = true))
        }

        val stepQuizNetworkError = stepQuizView.findViewById<View>(R.id.stepQuizNetworkError)
        stepQuizNetworkError.findViewById<View>(R.id.tryAgain).setOnClickListener {
            val quizMessage = StepQuizFeature.Message.InitWithStep(stepWrapper, lessonData, forceUpdate = true)
            stepQuizReviewTeacherViewModel
                .onNewMessage(StepQuizReviewTeacherFeature.Message.StepQuizMessage(quizMessage))
        }

        binding.stepQuizReviewTeacherSubmissions.setOnClickListener {
            parentFragment.safeCast<StepMenuNavigator>()
                ?.showSubmissions()
        }
    }

    override fun render(state: StepQuizReviewTeacherFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizReviewTeacherFeature.State.Data) {
            binding.stepQuizReviewTeacherContainer.isVisible =
                binding.stepQuizReviewTeacherArrow.isExpanded()

            quizViewStateDelegate.switchState(state.quizState)
            val stepQuizReviewTeacherMessage = binding.stepQuizReviewTeacherQuiz.findViewById<View>(R.id.stepQuizReviewTeacherMessage)
            stepQuizReviewTeacherMessage.isVisible = false
            if (state.quizState is StepQuizFeature.State.AttemptLoaded) {
                quizDelegate.setState(state.quizState)
            }

            binding.stepQuizReviewTeacherDescription.text =
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
        when (action) {
            StepQuizReviewTeacherFeature.Action.ViewAction.ShowNetworkError ->
                view?.snackbar(messageRes = R.string.no_connection)
        }
    }

    private fun syncReplyState(replyResult: ReplyResult) {
        stepQuizReviewTeacherViewModel.onNewMessage(
            StepQuizReviewTeacherFeature.Message.StepQuizMessage(
                StepQuizFeature.Message.SyncReply(replyResult.reply)))
    }
}
