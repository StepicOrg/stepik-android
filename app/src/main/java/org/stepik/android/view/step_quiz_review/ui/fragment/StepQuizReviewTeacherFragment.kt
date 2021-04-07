package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.BehaviorRelay
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
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz_review.ui.factory.StepQuizFormReviewFactory
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
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
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>

    @Inject
    internal lateinit var lessonData: LessonData

    private val stepQuizReviewTeacherViewModel: StepQuizReviewTeacherViewModel by reduxViewModel(this) { viewModelFactory }

    private var stepId: Long by argument()
    private var instructionType: ReviewStrategyType by argument()

    private lateinit var stepWrapper: StepPersistentWrapper

    private lateinit var quizView: View

    private lateinit var stepQuizFormFactory: StepQuizFormFactory

    private lateinit var quizDelegate: StepQuizDelegate

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
        val quizContainer = view.stepQuizReviewTeacherContainer
        quizView = inflater.inflate(stepQuizFormFactory.getLayoutResForStep(stepWrapper.step.block?.name), quizContainer, false)
        view.stepQuizReviewTeacherContainer.addView(quizView, 2)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val blockName = stepWrapper.step.block?.name

        stepQuizReviewTeacherContainer.isVisible = false

        stepQuizReviewTeacherSpoiler.setOnClickListener {
            stepQuizReviewTeacherArrow.changeState()
            if (stepQuizReviewTeacherArrow.isExpanded()) {
                stepQuizReviewTeacherContainer.expand()
            } else {
                stepQuizReviewTeacherContainer.collapse()
            }
        }

        val stepQuizBlockDelegate =
            StepQuizFeedbackBlocksDelegate(stepQuizReviewTeacherFeedback, isTeacher = false, hasReview = false) {}

        quizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = stepQuizFormFactory.getDelegateForStep(blockName, view) ?: throw IllegalStateException("Unsupported quiz"),
                stepQuizFeedbackBlocksDelegate = stepQuizBlockDelegate,

                stepQuizActionButton = stepQuizAction,
                stepRetryButton = stepQuizRetry,

                stepQuizDiscountingPolicy = stepQuizReviewTeacherDiscounting,
                stepQuizReviewTeacherMessage = null,
                onNewMessage = {
                    stepQuizReviewTeacherViewModel.onNewMessage(StepQuizReviewTeacherFeature.Message.StepQuizMessage(it))
                }
            )
    }

    override fun render(state: StepQuizReviewTeacherFeature.State) {
        if (state is StepQuizReviewTeacherFeature.State.Data) {
            if (state.quizState is StepQuizFeature.State.AttemptLoaded) {
                quizDelegate.setState(state.quizState)
            }

            stepQuizReviewTeacherMessage.text =
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