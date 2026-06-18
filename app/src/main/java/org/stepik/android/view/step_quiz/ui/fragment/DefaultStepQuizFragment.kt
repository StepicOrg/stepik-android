package org.stepik.android.view.step_quiz.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dev.androidbroadcast.vbpd.viewBinding
import com.jakewharton.rxrelay2.BehaviorRelay
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.databinding.FragmentStepQuizBinding
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.model.Step
import org.stepik.android.presentation.step_quiz.StepQuizViewModel
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.lesson.ui.interfaces.Moveable
import org.stepik.android.view.step.routing.StepDeepLinkBuilder
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.factory.StepQuizViewStateDelegateFactory
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

abstract class DefaultStepQuizFragment : Fragment(), ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction> {
    protected val stepQuizBinding: FragmentStepQuizBinding by viewBinding(FragmentStepQuizBinding::bind)

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepDeepLinkBuilder: StepDeepLinkBuilder

    @Inject
    internal lateinit var stepQuizViewStateDelegateFactory: StepQuizViewStateDelegateFactory

    protected lateinit var stepWrapper: StepPersistentWrapper

    @Inject
    internal lateinit var stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>
    @Inject
    internal lateinit var lessonData: LessonData

    protected var stepId: Long by argument()

    protected val viewModel: StepQuizViewModel by viewModels { viewModelFactory }

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizFeature.State>
    private lateinit var stepQuizDelegate: StepQuizDelegate

    protected abstract val quizViews: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        stepWrapper = stepWrapperRxRelay.value ?: throw IllegalStateException("Step wrapper cannot be null")

        viewModel.onNewMessage(StepQuizFeature.Message.InitWithStep(stepWrapper, lessonData))
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        (inflater.inflate(R.layout.fragment_step_quiz, container, false) as ViewGroup)
            .apply {
                addView(createStepView(inflater, this))
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewStateDelegate = stepQuizViewStateDelegateFactory.create(view, *quizViews)

        stepQuizBinding.stepQuizNetworkError.tryAgain.setOnClickListener {
            viewModel.onNewMessage(StepQuizFeature.Message.InitWithStep(stepWrapper, lessonData, forceUpdate = true))
        }

        stepQuizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = createStepQuizFormDelegate(),
                stepQuizFeedbackBlocksDelegate =
                    StepQuizFeedbackBlocksDelegate(
                        stepQuizBinding.stepQuizFeedbackBlocks.root,
                        lessonData.lesson.isTeacher,
                        stepWrapper.step.actions?.doReview != null
                    ) { openStepInWeb(stepWrapper.step) },
                stepQuizActionButton = stepQuizBinding.stepQuizActionContainer.stepQuizAction,
                stepRetryButton = stepQuizBinding.stepQuizActionContainer.stepQuizRetry,
                stepQuizDiscountingPolicy = stepQuizBinding.stepQuizDiscountingPolicy,
                stepQuizReviewTeacherMessage = stepQuizBinding.stepQuizReviewTeacherMessage,
                onNewMessage = viewModel::onNewMessage
            ) {
                (parentFragment as? Moveable)?.move()
            }
    }

    protected abstract fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View

    protected abstract fun createStepQuizFormDelegate(): StepQuizFormDelegate

    protected fun onActionButtonClicked() {
        stepQuizDelegate.onActionButtonClicked()
    }

    override fun onStart() {
        super.onStart()
        viewModel.attachView(this)
    }

    override fun onStop() {
        viewModel.detachView(this)
        super.onStop()
    }

    override fun render(state: StepQuizFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizFeature.State.AttemptLoaded) {
            stepQuizDelegate.setState(state)
        }
    }

    override fun onAction(action: StepQuizFeature.Action.ViewAction) {
        if (action is StepQuizFeature.Action.ViewAction.ShowNetworkError) {
            view?.snackbar(messageRes = R.string.no_connection)
        }
    }

    protected fun syncReplyState(replyResult: ReplyResult) {
        viewModel.onNewMessage(StepQuizFeature.Message.SyncReply(replyResult.reply))
    }

    private fun openStepInWeb(step: Step) {
        InAppWebViewDialogFragment
            .newInstance(lessonData.lesson.title.orEmpty(), stepDeepLinkBuilder.createStepLink(step), isProvideAuth = true)
            .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
    }
}
