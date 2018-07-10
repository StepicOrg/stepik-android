package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.R
import org.stepik.android.model.learning.attempts.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission

class NotSupportedQuizDelegate: QuizDelegate() {
    override var isEnabled: Boolean = false
    override var actionButton: Button? = null
        set(value) {
            value?.setText(R.string.open_web_to_solve)
            value?.setOnClickListener { }
        }

    override fun onCreateView(parent: ViewGroup): View = View(parent.context)

    override fun setAttempt(attempt: Attempt?) {}

    override fun setSubmission(submission: Submission?) {}

    override fun createReply(): Reply = Reply.Builder().build()
}