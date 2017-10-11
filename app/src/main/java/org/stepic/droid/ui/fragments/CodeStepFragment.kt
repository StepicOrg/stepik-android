package org.stepic.droid.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import javax.inject.Inject

class CodeStepFragment : StepAttemptFragment(), CodeView {

    companion object {
        private const val CHOSEN_POSITION_KEY: String = "chosenPositionKey"
        fun newInstance(): CodeStepFragment = CodeStepFragment()
    }

    @Inject
    lateinit var codePresenter: CodePresenter

    private val wrongAnswerTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            resetBackgroundOfAttempt()
            hideStatus()
        }

        override fun afterTextChanged(s: Editable?) {
            //no-op
        }

    }

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

            codeQuizAnswerField.text.clear()
            codeQuizAnswerField.setText(step.block?.options?.codeTemplates?.get(programmingLanguageServerName))
            showLanguageChoosingView(false)
            showCodeQuizEditor()
        }

        initLanguageChooser()

        codeQuizFullscreenAction.setOnClickListener {
            // TODO: 10/10/2017 implement
        }

        codeQuizResetAction.setOnClickListener {
            // TODO: 10/10/2017 implement
        }

        codeQuizAnswerField.addTextChangedListener(wrongAnswerTextWatcher)

        codePresenter.attachView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codePresenter.detachView(this)
        codeQuizAnswerField.removeTextChangedListener(wrongAnswerTextWatcher)
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
                    hideStatus()
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
        codeQuizAnswerField.text.clear()
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

}
