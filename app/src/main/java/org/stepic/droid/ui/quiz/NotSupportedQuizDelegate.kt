package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.R
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

class NotSupportedQuizDelegate: QuizDelegate() {
    override var isEnabled: Boolean = false
    override var actionButton: Button? = null
        set(value) {
            value?.setText(R.string.step_quiz_unsupported_action)
            value?.setOnClickListener { }
        }

    override fun onCreateView(parent: ViewGroup): View = View(parent.context)

    override fun setAttempt(attempt: Attempt?) {}

    override fun setSubmission(submission: Submission?) {}

    override fun createReply() = Reply()
}