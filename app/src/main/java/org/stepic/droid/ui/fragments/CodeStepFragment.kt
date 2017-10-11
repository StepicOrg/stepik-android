package org.stepic.droid.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.fragment_step_attempt.*
import kotlinx.android.synthetic.main.view_code_quiz.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.CodePresenter
import org.stepic.droid.core.presenters.contracts.CodeView
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import javax.inject.Inject

class CodeStepFragment : StepAttemptFragment(),
        CodeView,
        ResetCodeDialogFragment.Callback,
        ChangeCodeLanguageDialog.Callback {
    companion object {
        private const val CHOSEN_POSITION_KEY: String = "chosenPositionKey"
        fun newInstance(): CodeStepFragment = CodeStepFragment()
    }

    @Inject
    lateinit var codePresenter: CodePresenter

    override fun injectComponent() {
        App
                .componentManager()
                .stepComponent(step.id)
                .codeComponentBuilder()
                .build()
                .inject(this)
    }

    private var chosenProgrammingLanguageName: String? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewGroup = (this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.view_code_quiz, attemptContainer, false) as ViewGroup
        attemptContainer.addView(viewGroup)

        codeQuizChooseLangAction.setOnClickListener {
            val programmingLanguageServerName = codeQuizLanguagePicker.displayedValues[codeQuizLanguagePicker.value]
            chosenProgrammingLanguageName = programmingLanguageServerName

            codeQuizAnswerField.setText(step.block?.options?.codeTemplates?.get(programmingLanguageServerName))
            showLanguageChoosingView(false)
            showCodeQuizEditor()
        }

        initLanguageChooser()

        codeQuizFullscreenAction.setOnClickListener {
            // TODO: 10/10/2017 implement
        }

        codeQuizResetAction.setOnClickListener {
            if (checkForResetDialog()) {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }
        }

        codeQuizCurrentLanguage.setOnClickListener {
            if (checkForResetDialog()) {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            } else if (submission.status != Submission.Status.CORRECT) {
                onChangeLanguage()
            }
        }

        codePresenter.attachView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codePresenter.detachView(this)
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


    override fun generateReply(): Reply {
        return Reply.Builder()
                .setLanguage(chosenProgrammingLanguageName)
                .setCode(codeQuizAnswerField.text.toString())
                .build()
    }

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        codeQuizAnswerField.isEnabled = !needBlock
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
                if (submission.reply.code != codeQuizAnswerField.text.toString()) {
                    hideWrongStatus()
                    resetBackgroundOfAttempt()
                    hideHint()
                }
                submission = null
            } else {
                codeQuizAnswerField.setText(submission.reply.code)
                chosenProgrammingLanguageName = submission.reply.language
                showLanguageChoosingView(false)
                showCodeQuizEditor()
            }
        }
    }

    override fun onAttemptIsNotStored() {
        if (codeQuizAnswerField.visibility != View.VISIBLE) {
            showCodeQuizEditor(false)
            showLanguageChoosingView()
        }
    }

    override fun onShowStored(language: String, code: String) {
        chosenProgrammingLanguageName = language
        codeQuizAnswerField.setText(code)
        showLanguageChoosingView(false)
        showCodeQuizEditor()
    }


    private fun initLanguageChooser() {
        fun initView(languageNames: Array<String>) {
            codeQuizLanguagePicker.minValue = 0
            codeQuizLanguagePicker.maxValue = languageNames.size - 1
            codeQuizLanguagePicker.displayedValues = languageNames
            codeQuizLanguagePicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            codeQuizLanguagePicker.wrapSelectorWheel = false

            try {
                codeQuizLanguagePicker.setTextSize(50f) //Warning: reflection!
            } catch (exception: Exception) {
                //reflection failed -> ignore
            }
        }

        step.block?.options?.limits
                ?.keys
                ?.sorted()
                ?.toTypedArray()
                ?.let {
                    initView(it)
                }
    }

    private fun showCodeQuizEditor(needShow: Boolean = true) {
        val visibility = toVisibility(needShow)
        if (needShow) {
            codeQuizCurrentLanguage.text = chosenProgrammingLanguageName
        }

        codeQuizAnswerField.visibility = visibility
        stepAttemptSubmitButton.visibility = visibility
        codeQuizCurrentLanguage.visibility = visibility
        codeQuizDelimiter.visibility = visibility
        codeQuizFullscreenAction.visibility = visibility
        codeQuizResetAction.visibility = visibility
    }

    private fun showLanguageChoosingView(needShow: Boolean = true) {
        val visibility = toVisibility(needShow)

        codeQuizChooseLangAction.visibility = visibility
        codeQuizLanguagePicker.visibility = visibility
        codeQuizChooseLangTitle.visibility = visibility
    }

    override fun onReset() {
        chosenProgrammingLanguageName?.let {
            val template = step?.block?.options?.codeTemplates?.get(it)
            codeQuizAnswerField.setText(template)
            resetBackgroundOfAttempt()
            hideHint()
            hideWrongStatus()
        }
    }

    override fun onChangeLanguage() {
        showCodeQuizEditor(false)
        showLanguageChoosingView()
    }

    private fun checkForResetDialog(): Boolean {
        chosenProgrammingLanguageName?.let {
            val template = step.block?.options?.codeTemplates?.get(it)
            val needDialog = template != null &&
                    template != codeQuizAnswerField.text.toString() &&
                    submission?.status != Submission.Status.CORRECT
            return needDialog
        }
        return false
    }

}
