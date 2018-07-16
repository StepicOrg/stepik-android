package org.stepic.droid.ui.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import org.stepic.droid.R
import org.stepik.android.model.learning.attempts.Attempt
import org.stepic.droid.model.Submission
import org.stepik.android.model.learning.replies.Reply

open class StringQuizDelegate: QuizDelegate() {
    protected lateinit var answerField: EditText

    override var isEnabled: Boolean
        get() = answerField.isEnabled
        set(value) {
            answerField.isEnabled = value
        }

    override var actionButton: Button? = null

    override fun onCreateView(parent: ViewGroup): View =
            LayoutInflater.from(parent.context).inflate(R.layout.view_free_answer_attempt, parent, false)

    override fun onViewCreated(view: View) {
        answerField = view as EditText
    }

    override fun setAttempt(attempt: Attempt?) {
        answerField.text.clear()
    }

    override fun setSubmission(submission: Submission?) {
        submission?.reply?.text?.let { answerField.setText(it) }
    }

    override fun createReply() = Reply(text = answerField.text.toString())
}