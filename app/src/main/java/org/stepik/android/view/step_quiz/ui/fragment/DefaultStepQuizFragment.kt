package org.stepik.android.view.step_quiz.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.model.Step
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.lesson.ui.interfaces.NextMoveable
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import org.stepik.android.view.step.routing.StepDeepLinkBuilder
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

abstract class DefaultStepQuizFragment : Fragment(), StepQuizView {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepDeepLinkBuilder: StepDeepLinkBuilder

    internal lateinit var stepWrapper: StepPersistentWrapper

    @Inject
    internal lateinit var stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>
    @Inject
    internal lateinit var lessonData: LessonData

    protected var stepId: Long by argument()

    private lateinit var presenter: StepQuizPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizView.State>
    private lateinit var stepQuizDelegate: StepQuizDelegate

    protected abstract val quizLayoutRes: Int
        @LayoutRes get

    protected abstract val quizViews: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        stepWrapper = stepWrapperRxRelay.value ?: throw IllegalStateException("Step wrapper cannot be null")

        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(StepQuizPresenter::class.java)
        presenter.onStepData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        (inflater.inflate(R.layout.fragment_step_quiz, container, false) as ViewGroup)
            .apply {
                addView(inflater.inflate(quizLayoutRes, this, false))
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizView.State.Idle>()
        viewStateDelegate.addState<StepQuizView.State.Loading>(stepQuizProgress)
        viewStateDelegate.addState<StepQuizView.State.AttemptLoaded>(stepQuizDiscountingPolicy, stepQuizFeedbackBlocks, stepQuizDescription, stepQuizActionContainer, *quizViews)
        viewStateDelegate.addState<StepQuizView.State.NetworkError>(stepQuizNetworkError)

        stepQuizNetworkError.tryAgain.setOnClickListener { presenter.onStepData(stepWrapper, lessonData, forceUpdate = true) }

        stepQuizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizLessonData = StepQuizLessonData(lessonData),
                stepQuizFormDelegate = createStepQuizFormDelegate(view),
                stepQuizFeedbackBlocksDelegate =
                    StepQuizFeedbackBlocksDelegate(
                        stepQuizFeedbackBlocks,
                        stepWrapper.step.actions?.doReview != null
                    ) { openStepInWeb(stepWrapper.step) },
                stepQuizActionButton = stepQuizAction,
                stepRetryButton = stepQuizRetry,
                stepQuizDiscountingPolicy = stepQuizDiscountingPolicy,
                stepQuizPresenter = presenter
            ) {
                (parentFragment as? NextMoveable)?.moveNext()
            }
    }

    protected abstract fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate

    protected fun onActionButtonClicked() {
        stepQuizDelegate.onActionButtonClicked()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        stepQuizDelegate.syncReplyState()
        super.onStop()
    }

    override fun setState(state: StepQuizView.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizView.State.AttemptLoaded) {
            stepQuizDelegate.setState(state)
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.no_connection)
    }

    private fun openStepInWeb(step: Step) {
        MagicLinkDialogFragment
            .newInstance(stepDeepLinkBuilder.createStepLink(step))
            .showIfNotExists(childFragmentManager, MagicLinkDialogFragment.TAG)
    }
}