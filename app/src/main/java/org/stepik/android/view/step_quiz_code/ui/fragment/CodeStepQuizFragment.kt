package org.stepik.android.view.step_quiz_code.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.fragment_step_quiz.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.listeners.NextMoveable
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CodeStepQuizFragment : Fragment(), StepQuizView, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, CodeStepQuizFullScreenDialogFragment.Callback {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            CodeStepQuizFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var fontsProvider: FontsProvider

    @Inject
    internal lateinit var screenManager: ScreenManager

    private lateinit var presenter: StepQuizPresenter

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizView.State>
    private lateinit var stepQuizDelegate: StepQuizDelegate

    private lateinit var codeStepQuizFormDelegate: CodeStepQuizFormDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        presenter = ViewModelProviders.of(this, viewModelFactory).get(StepQuizPresenter::class.java)
        presenter.onStepData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        (inflater.inflate(R.layout.fragment_step_quiz, container, false) as ViewGroup)
            .apply {
                addView(inflater.inflate(R.layout.layout_step_quiz_code, this, false))
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stepQuizDescription.visibility = View.GONE

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizView.State.Idle>()
        viewStateDelegate.addState<StepQuizView.State.Loading>(stepQuizProgress)
        viewStateDelegate.addState<StepQuizView.State.AttemptLoaded>(stepQuizDiscountingPolicy, stepQuizFeedbackBlocks, stepQuizCodeContainer, stepQuizActionContainer)
        viewStateDelegate.addState<StepQuizView.State.NetworkError>(stepQuizNetworkError)

        stepQuizNetworkError.tryAgain.setOnClickListener { presenter.onStepData(stepWrapper, lessonData, forceUpdate = true) }

        val actionsListener = object : CodeStepQuizFormDelegate.ActionsListener {
            override fun onChangeLanguageClicked() {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }

            override fun onFullscreenClicked(lang: String, code: String) {
                val supportFragmentManager = fragmentManager
                    ?.takeIf { it.findFragmentByTag(CodeStepQuizFullScreenDialogFragment.TAG) == null }
                    ?: return

                val dialog = CodeStepQuizFullScreenDialogFragment.newInstance(lang, code, stepWrapper, lessonData)
                dialog.setTargetFragment(this@CodeStepQuizFragment, CodeStepQuizFullScreenDialogFragment.CODE_PLAYGROUND_REQUEST)
                dialog.show(supportFragmentManager, CodeStepQuizFullScreenDialogFragment.TAG)
            }
        }

        codeStepQuizFormDelegate = CodeStepQuizFormDelegate(view, stepWrapper, actionsListener)

        stepQuizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                lessonData = lessonData,
                stepQuizFormDelegate = codeStepQuizFormDelegate,
                stepQuizFeedbackBlocksDelegate = StepQuizFeedbackBlocksDelegate(
                    stepQuizFeedbackBlocks,
                    fontsProvider,
                    stepWrapper.step.actions?.doReview != null,
                    { screenManager.openStepInWeb(requireContext(), stepWrapper.step) }
                ),
                stepQuizActionButton = stepQuizAction,
                stepRetryButton = stepQuizRetry,
                stepQuizDiscountingPolicy = stepQuizDiscountingPolicy,
                stepQuizPresenter = presenter
            ) {
                (parentFragment as? NextMoveable)?.moveNext()
            }
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
        val view = view ?: return

        Snackbar
            .make(view, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    override fun onChangeLanguage() {
        val languages = stepWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        codeStepQuizFormDelegate.onLanguageSelected(programmingLanguage)
    }

    override fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean) {
        codeStepQuizFormDelegate.updateCodeLayout(lang, code)
        if (onSubmitClicked) {
            stepQuizDelegate.onActionButtonClicked()
        }
    }
}