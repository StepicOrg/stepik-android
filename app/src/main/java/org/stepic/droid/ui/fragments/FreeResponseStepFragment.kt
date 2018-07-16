package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import org.stepic.droid.R
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.replies.Reply

class FreeResponseStepFragment: StepAttemptFragment() {

    lateinit var answerField: EditText

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        answerField = layoutInflater.inflate(R.layout.view_free_answer_attempt, attemptContainer, false) as EditText
        attemptContainer.addView(answerField)
    }

    override fun showAttempt(attempt: Attempt) {
        //do nothing, because this attempt doesn't have any specific.
        answerField.text.clear()
    }

    override fun generateReply(): Reply {
        var answer = answerField.text.toString()
        if (attempt?.getDataset()?.isHtmlEnabled == true) {
            answer = textResolver.replaceWhitespaceToBr(answer)
        }

        return Reply(text = answer, attachments = emptyList())
    }

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        answerField.isEnabled = !needBlock
    }

    override fun onRestoreSubmission() {
        val reply = submission.reply ?: return

        val text = reply.text
        if (attempt?.getDataset()?.isHtmlEnabled == true) {
            //todo show as html in enhanced latexview
            answerField.setText(textResolver.fromHtml(text))
        } else {
            answerField.setText(text)
        }
    }

    override fun getCorrectString(): String = getString(R.string.correct_free_response)

    override fun onPause() {
        super.onPause()
        answerField.clearFocus()
    }
}