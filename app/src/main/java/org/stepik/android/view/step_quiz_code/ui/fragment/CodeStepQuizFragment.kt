package org.stepik.android.view.step_quiz_code.ui.fragment

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
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
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.base.ui.extension.parentOfType
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.activity.CodeStepQuizFullScreenActivity
import org.stepik.android.view.step_quiz_fullscreen_code.ui.fragment.CodeStepQuizFullScreenPlaygroundFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CodeStepQuizFragment : Fragment(), StepQuizView, ResetCodeDialogFragment.Callback, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback {
    companion object {
        private const val CODE_PLAYGROUND_REQUEST = 153

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

    private lateinit var presenter: StepQuizPresenter

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizView.State>
    private lateinit var stepQuizDelegate: StepQuizDelegate

    private lateinit var codeStepQuizFormDelegate: CodeStepQuizFormDelegate

    private var isFullScreenSubmitClicked: Boolean = false

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
        viewStateDelegate.addState<StepQuizView.State.AttemptLoaded>(stepQuizDiscountingPolicy, stepQuizFeedbackBlocks, stepQuizCodeContainer, stepQuizAction)
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
                stepQuizDelegate.syncReplyState {
                    val intent = CodeStepQuizFullScreenActivity.createIntent(requireActivity(), lang, stepWrapper, lessonData)
                    startActivityForResult(intent, CODE_PLAYGROUND_REQUEST)
                }
            }

            override fun onResetClicked() {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }
        }

        codeStepQuizFormDelegate = CodeStepQuizFormDelegate(view, parentOfType(), stepWrapper, actionsListener)

        stepQuizDelegate =
            StepQuizDelegate(
                step = stepWrapper.step,
                stepQuizFormDelegate = codeStepQuizFormDelegate,
                stepQuizFeedbackBlocksDelegate = StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks, fontsProvider),
                stepQuizActionButton = stepQuizAction,
                stepQuizDiscountingPolicy = stepQuizDiscountingPolicy,
                stepQuizPresenter = presenter
            )
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
            if (isFullScreenSubmitClicked && state.submissionState is StepQuizView.SubmissionState.Loaded) {
                isFullScreenSubmitClicked = false
                stepQuizDelegate.onActionButtonClicked()
            }
        }
    }

    override fun showNetworkError() {
        val view = view ?: return

        Snackbar
            .make(view, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    override fun onReset() {
        codeStepQuizFormDelegate.onResetCode()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODE_PLAYGROUND_REQUEST ->
                if (resultCode == Activity.RESULT_OK) {
                    isFullScreenSubmitClicked = data?.getBooleanExtra(CodeStepQuizFullScreenPlaygroundFragment.IS_SUBMITTED_CLICKED, false) ?: false
                    presenter.fetchUponFullScreenReturn(stepWrapper, lessonData)
                }
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }
}