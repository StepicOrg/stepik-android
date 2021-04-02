package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.BehaviorRelay
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.presentation.step_quiz.model.ReplyResult
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

    private val stepQuizFormFactory =
        StepQuizFormReviewFactory(childFragmentManager, ::syncReplyState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()
        stepWrapper = stepWrapperRxRelay.value ?: throw IllegalStateException("Step wrapper cannot be null")
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        quizView = inflater.inflate(stepQuizFormFactory.getLayoutResForStep(stepWrapper.step.block?.name), null)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun syncReplyState(replyResult: ReplyResult) {
//        stepQuizReviewViewModel.onNewMessage(
//            StepQuizReviewFeature.Message.StepQuizMessage(
//                StepQuizFeature.Message.SyncReply(replyResult.reply)))
    }
}