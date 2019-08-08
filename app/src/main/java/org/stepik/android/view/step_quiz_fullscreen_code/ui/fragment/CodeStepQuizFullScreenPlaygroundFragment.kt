package org.stepik.android.view.step_quiz_fullscreen_code.ui.fragment

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.base.ui.extension.parentOfType
import org.stepik.android.view.step_quiz_fullscreen_code.ui.delegate.CodeQuizFormFullScreenDelegate
import javax.inject.Inject

class CodeStepQuizFullScreenPlaygroundFragment : Fragment(), StepQuizView, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, ResetCodeDialogFragment.Callback, OnBackClickListener {
    companion object {
        const val IS_SUBMITTED_CLICKED = "is_submit_clicked"

        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            CodeStepQuizFullScreenPlaygroundFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: StepQuizPresenter

    private lateinit var codeStepQuizFormFullScreenDelegate: CodeQuizFormFullScreenDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setHasOptionsMenu(true)
        presenter = ViewModelProviders.of(this, viewModelFactory).get(StepQuizPresenter::class.java)
        presenter.onStepData(stepWrapper, lessonData)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_code_fullscreen_playground, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionsListener = object : CodeQuizFormFullScreenDelegate.ActionsListener {
            override fun onSubmitClicked() {
                syncCodeOnFullScreenExit(true)
            }
        }

        codeStepQuizFormFullScreenDelegate = CodeQuizFormFullScreenDelegate(view, parentOfType(), stepWrapper, actionsListener)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as? BackButtonHandler)?.setBackClickListener(this)
    }

    override fun onDetach() {
        (activity as? BackButtonHandler)?.removeBackClickListener(this)
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        val reply = codeStepQuizFormFullScreenDelegate.createReply()
        if (reply is ReplyResult.Success) {
            presenter.syncReplyState(reply.reply)
        }
        super.onStop()
    }

    override fun setState(state: StepQuizView.State) {
        if (state is StepQuizView.State.AttemptLoaded) {
            codeStepQuizFormFullScreenDelegate.setState(state)
        }
    }

    override fun showNetworkError() {
        val view = view ?: return

        Snackbar
            .make(view, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    override fun onBackClick(): Boolean {
        syncCodeOnFullScreenExit()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.code_playground_menu, menu)
        val menuItem = menu.findItem(R.id.action_reset_code)
        val resetString = SpannableString(getString(R.string.code_quiz_reset))
        resetString.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.new_red_color)), 0, resetString.length, 0)
        menuItem?.title = resetString
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            R.id.action_reset_code -> {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
                true
            }
            R.id.action_language_code -> {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
                true
            }
            else -> false
        }

    override fun onChangeLanguage() {
        val languages = stepWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        codeStepQuizFormFullScreenDelegate.onLanguageSelected(programmingLanguage)
    }

    override fun onReset() {
        codeStepQuizFormFullScreenDelegate.onResetCode()
    }

    private fun syncCodeOnFullScreenExit(isSubmittedClicked: Boolean = false) {
        val reply = codeStepQuizFormFullScreenDelegate.createReply()
        if (reply is ReplyResult.Success) {
            presenter.syncReplyState(reply.reply) {
                (activity as? BackButtonHandler)?.removeBackClickListener(this)
                val data = Intent()
                data.putExtra(IS_SUBMITTED_CLICKED, isSubmittedClicked)
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.onBackPressed()
            }
        }
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }
}