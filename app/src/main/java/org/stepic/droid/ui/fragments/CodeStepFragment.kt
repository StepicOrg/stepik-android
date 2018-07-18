package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.fragment_step_attempt.*
import kotlinx.android.synthetic.main.view_code_editor_layout.*
import kotlinx.android.synthetic.main.view_code_quiz.*
import kotlinx.android.synthetic.main.view_code_toolbar.*
import kotlinx.android.synthetic.main.view_step_preparing.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.core.presenters.CodePresenter
import org.stepic.droid.core.presenters.PreparingCodeStepPresenter
import org.stepic.droid.core.presenters.contracts.CodeView
import org.stepic.droid.core.presenters.contracts.PreparingCodeStepView
import org.stepik.android.model.structure.Step
import org.stepic.droid.model.Submission
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.ui.activities.CodePlaygroundActivity
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.initForCodeLanguages
import org.stepic.droid.ui.util.listenKeyboardChanges
import org.stepic.droid.ui.util.stopListenKeyboardChanges
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.replies.Reply
import javax.inject.Inject

class CodeStepFragment : StepAttemptFragment(),
        CodeView,
        PreparingCodeStepView,
        ResetCodeDialogFragment.Callback,
        ChangeCodeLanguageDialog.Callback, CodeToolbarAdapter.OnSymbolClickListener {
    companion object {
        private const val ANALYTIC_SCREEN_TYPE = "standard"
        private const val CHOSEN_POSITION_KEY: String = "chosenPositionKey"
        private const val CODE_PLAYGROUND_REQUEST = 153
        fun newInstance(): CodeStepFragment = CodeStepFragment()
    }

    @Inject
    lateinit var codePresenter: CodePresenter

    @Inject
    lateinit var preparingCodeStepPresenter: PreparingCodeStepPresenter

    private var codeToolbarAdapter: CodeToolbarAdapter? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun injectComponent() {
        App
                .componentManager()
                .stepComponent(step.id)
                .codeComponentBuilder()
                .build()
                .inject(this)
    }

    private var chosenProgrammingLanguageName: String? = null
        set(value) {
            field = value
            if (value != null) codeEditor?.let { it.lang = extensionForLanguage(value) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeToolbarAdapter = CodeToolbarAdapter(context)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewGroup = (this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.view_code_quiz, attemptContainer, false) as ViewGroup
        attemptContainer.addView(viewGroup)

        codeToolbarView.adapter = codeToolbarAdapter
        codeToolbarAdapter?.onSymbolClickListener = this
        codeToolbarView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        codeEditor.codeToolbarAdapter = codeToolbarAdapter

        codePresenter.attachView(this)
        preparingCodeStepPresenter.attachView(this)
        preparingCodeStepPresenter.prepareStep(step)
    }

    override fun onStepPrepared(newStep: Step) {
        //here code options should be prepared
        step = newStep

        initLanguageChooser()

        codeQuizChooseLangAction.setOnClickListener {
            val programmingLanguageServerName = codeQuizLanguagePicker?.displayedValues?.get(codeQuizLanguagePicker.value)
            if (programmingLanguageServerName == null) {
                analytic.reportEventWithName(Analytic.Code.CHOOSE_NULL, "${step?.lesson} ${step?.id}")
                return@setOnClickListener
            }

            chosenProgrammingLanguageName = programmingLanguageServerName

            codeEditor.setText(step.block?.options?.codeTemplates?.get(programmingLanguageServerName))
            showLanguageChoosingView(false)
            showCodeQuizEditor()
        }

        codeQuizFullscreenAction.setOnClickListener {
            chosenProgrammingLanguageName?.let { lang ->
                if (submission?.status != Submission.Status.CORRECT) {
                    val intent = CodePlaygroundActivity.intentForLaunch(
                            activity,
                            codeEditor.text.toString(),
                            lang,
                            step?.block?.options ?: throw IllegalStateException("can't find code options in code quiz"))

                    analytic.reportEvent(Analytic.Code.CODE_FULLSCREEN_PRESSED)
                    startActivityForResult(intent, CODE_PLAYGROUND_REQUEST)
                }
            }
        }

        codeQuizResetAction.setOnClickListener {
            analytic.reportEvent(Analytic.Code.CODE_RESET_PRESSED,
                    Bundle().apply { putString(AppConstants.ANALYTIC_CODE_SCREEN_KEY, ANALYTIC_SCREEN_TYPE) }
            )

            if (submission?.status == Submission.Status.CORRECT) {
                tryAgain()
            } else {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }

        }

        codeQuizCurrentLanguage.setOnClickListener {
            if (isOneLanguageAvailable()) {
                //we shouldn't change it
                return@setOnClickListener
            }

            if (checkForResetDialog()) {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            } else if (submission?.status != Submission.Status.CORRECT) {
                onChangeLanguage()
            }
        }

        initSamples()

        ProgressHelper.dismiss(progressBar)
        stepPreparingView.visibility = View.GONE
        codePreparedContainer.visibility = View.VISIBLE
        if (chosenProgrammingLanguageName == null) {
            showCodeQuizEditor(false)
            showLanguageChoosingView(true)
        } else {
            showCodeQuizEditor(true)
            showLanguageChoosingView(false)
        }
    }

    override fun onStepNotPrepared() {
        showCodeQuizEditor(false)
        showLanguageChoosingView(false)
        ProgressHelper.dismiss(progressBar)
        codePreparedContainer.visibility = View.GONE

        stepPreparingView.visibility = View.VISIBLE
        stepPreparingView.setOnClickListener {
            stepPreparingView.visibility = View.GONE
            preparingCodeStepPresenter.prepareStep(step)
        }
    }

    override fun onStepPreparing() {
        //note: on 1st load this progress bar can be hidden by attempt is loading
        //if we introduce progress bar for step loading, then we will have 2 progress bar on screen
        //todo sync dismissing of progressbar in future
        ProgressHelper.activate(progressBar)
        stepAttemptSubmitButton.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        onGlobalLayoutListener = listenKeyboardChanges(
                rootFrameLayoutInStepAttempt,
                onKeyboardShown = {
                    codeToolbarView.visibility = View.VISIBLE
                    codeToolbarSpaceInContainer.visibility = View.VISIBLE
                },
                onKeyboardHidden = {
                    codeToolbarView.visibility = View.GONE
                    codeToolbarSpaceInContainer.visibility = View.GONE
                }
        )
    }

    override fun onStop() {
        super.onStop()
        stopListenKeyboardChanges(rootFrameLayoutInStepAttempt, onGlobalLayoutListener)
        onGlobalLayoutListener = null
    }

    private fun initSamples() {
        val newLine = "<br>"
        val stringBuilder = StringBuilder()
        step?.block?.options?.samples?.forEachIndexed { index, parcelableStringList ->
            if (index != 0) {
                stringBuilder.append(newLine)
                stringBuilder.append(newLine)
            }
            stringBuilder.append(getString(R.string.sample_input, index + 1))
            stringBuilder.append(newLine)
            stringBuilder.append(parcelableStringList[0].replace("\n", newLine))
            stringBuilder.append(newLine)
            stringBuilder.append(getString(R.string.sample_output, index + 1))
            stringBuilder.append(newLine)
            stringBuilder.append(parcelableStringList[1].replace("\n", newLine))
        }
        val samplesString = textResolver.fromHtml(stringBuilder.toString())
        codeQuizSamples.text = samplesString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preparingCodeStepPresenter.detachView(this)
        codePresenter.detachView(this)
        codeToolbarAdapter?.onSymbolClickListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        codeToolbarAdapter = null
    }

    override fun showAttempt(attempt: Attempt) {
        codePresenter.onShowAttempt(attemptId = attempt.id, stepId = step.id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHOSEN_POSITION_KEY, codeQuizLanguagePicker.value)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            codeQuizLanguagePicker.value = it.getInt(CHOSEN_POSITION_KEY)
        }
    }

    private fun toVisibility(needShow: Boolean): Int {
        return if (needShow) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun generateReply(): Reply = Reply(
            language = chosenProgrammingLanguageName,
            code = codeEditor.text.toString()
    )


    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        codeEditor.isEnabled = !needBlock
    }

    override fun onRestoreSubmission() {
        if (submission?.reply == null) {
            if (chosenProgrammingLanguageName == null) {
                //we haven't submission from the server and stored submission
                showLanguageChoosingView(false)
                showCodeQuizEditor()
            }
        } else {
            if (submission.status == Submission.Status.WRONG) {
                actionButton.setText(R.string.send)
                blockUIBeforeSubmit(false)
                if (submission.reply.code != codeEditor.text.toString()) {
                    hideWrongStatus()
                    resetBackgroundOfAttempt()
                    hideHint()
                }
                submission = null
            } else {
                codeEditor.setText(submission.reply.code)
                chosenProgrammingLanguageName = submission.reply.language
                showLanguageChoosingView(false)
                showCodeQuizEditor()
            }
        }
    }

    override fun onAttemptIsNotStored() {
        if (codeEditor.visibility == View.VISIBLE) {
            return
        }

        if (isOneLanguageAvailable()) {
            val langTemplate = step?.block?.options?.codeTemplates?.entries?.singleOrNull()
            if (langTemplate == null) {
                analytic.reportEvent(Analytic.Error.TEMPLATE_WAS_NULL, step.id.toString())
                return
            }
            chosenProgrammingLanguageName = langTemplate.key
            codeEditor.setText(langTemplate.value)

            showLanguageChoosingView(false)
            showCodeQuizEditor()
        } else {
            showCodeQuizEditor(false)
            showLanguageChoosingView()
        }
    }

    override fun onShowStored(language: String, code: String) {
        chosenProgrammingLanguageName = language
        codeEditor.setText(code)
        showLanguageChoosingView(false)
        showCodeQuizEditor()
    }

    private fun initLanguageChooser() {
        step.block?.options?.limits
                ?.keys
                ?.sorted()
                ?.toTypedArray()
                ?.let {
                    codeQuizLanguagePicker.initForCodeLanguages(it)
                }
    }

    private fun showCodeQuizEditor(needShow: Boolean = true) {
        val visibility = toVisibility(needShow)
        if (needShow) {
            chosenProgrammingLanguageName?.let { chosenLanguage ->
                codeToolbarAdapter?.setLanguage(chosenLanguage)
                codeQuizCurrentLanguage.text = chosenLanguage
                step?.block?.options?.limits?.get(chosenLanguage)?.let { codeLimit ->
                    val stringFromResources = getString(R.string.code_quiz_limits,
                            resources.getQuantityString(R.plurals.time_seconds, codeLimit.time, codeLimit.time),
                            codeLimit.memory.toString())
                    val spanned = textResolver.fromHtml(stringFromResources)
                    codeQuizLimits.text = spanned
                }
            }
        }

        if (codePreparedContainer.visibility != View.VISIBLE) {
            return
        }

        codeEditor.visibility = visibility
        stepAttemptSubmitButton.visibility = visibility
        codeQuizCurrentLanguage.visibility = visibility
        codeQuizDelimiter.visibility = visibility
        codeQuizFullscreenAction.visibility = visibility
        codeQuizResetAction.visibility = visibility
        codeQuizLimits.visibility = visibility
    }

    private fun showLanguageChoosingView(needShow: Boolean = true) {
        val visibility = toVisibility(needShow)

        if (needShow) {
            chosenProgrammingLanguageName = null
        }
        if (codePreparedContainer.visibility != View.VISIBLE) {
            return
        }

        codeQuizChooseLangAction.visibility = visibility
        codeQuizLanguagePicker.visibility = visibility
        codeQuizChooseLangTitle.visibility = visibility
    }

    override fun onReset() {
        chosenProgrammingLanguageName?.let {
            val template = step?.block?.options?.codeTemplates?.get(it)
            codeEditor.setText(template)
            resetBackgroundOfAttempt()
            hideHint()
            hideWrongStatus()
        }
    }

    override fun onChangeLanguage() {
        if (isOneLanguageAvailable()) {
            //we shouldn't do anything
            return
        }
        showCodeQuizEditor(false)
        showLanguageChoosingView()
        resetBackgroundOfAttempt()
        hideHint()
        hideWrongStatus()
    }

    private fun checkForResetDialog(): Boolean {
        chosenProgrammingLanguageName?.let {
            val template = step.block?.options?.codeTemplates?.get(it)
            val needDialog = template != null &&
                    template != codeEditor.text.toString() &&
                    submission?.status != Submission.Status.CORRECT
            return needDialog
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CODE_PLAYGROUND_REQUEST && resultCode == Activity.RESULT_OK) {
            chosenProgrammingLanguageName = data?.getStringExtra(CodePlaygroundActivity.LANG_KEY)
            val newCode = data?.getStringExtra(CodePlaygroundActivity.CODE_KEY)
            codeEditor.setText(newCode)
            showCodeQuizEditor()
            if (data?.getBooleanExtra(CodePlaygroundActivity.WAS_RESET, false) == true) {
                resetBackgroundOfAttempt()
                hideHint()
                hideWrongStatus()
            }
        }
    }

    private fun isOneLanguageAvailable(): Boolean =
            step?.block?.options?.codeTemplates?.size == 1

    override fun onSymbolClick(symbol: String) {
        CodeToolbarUtil.reportSelectedSymbol(analytic, chosenProgrammingLanguageName, symbol)
        codeEditor.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeEditor.indentSize))
    }

}
