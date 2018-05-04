package org.stepic.droid.ui.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.R
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission

class NotSupportedQuizDelegate: QuizDelegate {
    override var isEnabled: Boolean = false
    override var actionButton: Button? = null

    override fun onCreateView(parent: ViewGroup): View =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_unsupported, parent, false)

    override fun setAttempt(attempt: Attempt?) {}

    override fun setSubmission(submission: Submission?) {}

    override fun createReply(): Reply = Reply.Builder().build()
}