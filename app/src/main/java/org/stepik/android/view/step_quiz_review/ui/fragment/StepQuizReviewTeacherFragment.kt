package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFormFactory
import org.stepik.android.view.step_quiz_review.ui.factory.StepQuizFormReviewFactory
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class StepQuizReviewTeacherFragment : Fragment() {
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

    private var stepId: Long by argument()
    private var instructionType: ReviewStrategyType by argument()

    private lateinit var stepWrapper: StepPersistentWrapper

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
        val view = inflater.inflate(R.layout.fragment_step_quiz_review_teacher, container, false)
        val quizContainer = view.stepQuizReviewTeacherContainer
        quizView = inflater.inflate(stepQuizFormFactory.getLayoutResForStep(stepWrapper.step.block?.name), quizContainer, false)
        view.stepQuizReviewTeacherContainer.addView(quizView, 0)
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

        val quizDelegate =
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
                    // stepQuizReviewViewModel.onNewMessage(StepQuizReviewFeature.Message.StepQuizMessage(it))
                }
            )
    }

    private fun syncReplyState(replyResult: ReplyResult) {
//        stepQuizReviewViewModel.onNewMessage(
//            StepQuizReviewFeature.Message.StepQuizMessage(
//                StepQuizFeature.Message.SyncReply(replyResult.reply)))
    }
}