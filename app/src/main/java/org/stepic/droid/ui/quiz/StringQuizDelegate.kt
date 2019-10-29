package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

open class StringQuizDelegate: QuizDelegate() {
    protected lateinit var answerField: EditText

    override var isEnabled: Boolean
        get() = answerField.isEnabled
        set(value) {
            answerField.isEnabled = value
        }

    override var actionButton: Button? = null

    override fun onCreateView(parent: ViewGroup): View =
        parent.inflate(R.layout.view_free_answer_attempt)

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