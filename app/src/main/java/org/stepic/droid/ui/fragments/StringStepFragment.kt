package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import org.stepic.droid.R
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.Reply

class StringStepFragment: StepAttemptFragment() {

    private lateinit var answerField: EditText

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        answerField = layoutInflater.inflate(R.layout.view_free_answer_attempt, attemptContainer, false) as EditText
        attemptContainer.addView(answerField)
    }

    override fun showAttempt(attempt: Attempt) {
        answerField.text.clear()
    }

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        answerField.isEnabled = !needBlock
    }

    override fun generateReply(): Reply =
            Reply(text = answerField.text.toString())

    override fun onRestoreSubmission() {
        val text = submission.reply?.text ?: return
        answerField.setText(text)
    }

    override fun onPause() {
        super.onPause()
        answerField.clearFocus()
    }
}