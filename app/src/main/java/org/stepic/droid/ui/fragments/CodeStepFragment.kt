package org.stepic.droid.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.code_attempt.*
import org.stepic.droid.R
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply

class CodeStepFragment : StepAttemptFragment() {

    companion object {
        fun newInstance(): CodeStepFragment = CodeStepFragment()
    }

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
