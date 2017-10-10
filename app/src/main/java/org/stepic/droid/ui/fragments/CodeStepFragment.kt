package org.stepic.droid.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.code_attempt.*
import org.stepic.droid.R
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.code.ProgrammingLanguage

class CodeStepFragment : StepAttemptFragment() {

    companion object {
        private const val CHOSEN_POSITION_KEY: String = "chosenPositionKey"
        fun newInstance(): CodeStepFragment = CodeStepFragment()
    }

    private var chosenProgrammingLanguage: ProgrammingLanguage? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewGroup = (this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.code_attempt, attemptContainer, false) as ViewGroup
        attemptContainer.addView(viewGroup)
    }

    override fun showAttempt(attempt: Attempt) {
        //do nothing, because this attempt doesn't have any specific.
        codeQuizAnswerField.text.clear()
        // TODO: 29.03.16  choose and after that get from step.block.options.code_templates or from stored submission
        codeQuizAnswerField.setText(textResolver.fromHtml("#include <iostream> int main() { // put your code here return 0; }"))

        step.block?.options?.limits
                ?.keys
                ?.map {
                    it.printableName
                }
                ?.sorted()
                ?.toTypedArray()
                ?.let {
                    showLanguageChooser(it)
                }

    }

    private fun showLanguageChooser(languageNames: Array<String>) {
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

    override fun generateReply(): Reply {
        return Reply.Builder()
                .setLanguage("c++11") // TODO: 29.03.16 choose and after that get from step.block.options.limits
                .setCode(codeQuizAnswerField.text.toString())
                .build()
    }

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        codeQuizAnswerField.isEnabled = !needBlock
    }

    override fun onRestoreSubmission() {
        val reply = submission.reply ?: return

        val text = reply.code
        codeQuizAnswerField.setText(textResolver.fromHtml(text)) // TODO: 29.03.16 render code
    }
}
