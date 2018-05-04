package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission

abstract class QuizViewDelegate {
    abstract var isEnabled: Boolean
    abstract var actionButton: Button?

    abstract fun onCreateView(parent: ViewGroup): View
    open fun onViewCreated(view: View) {}

    abstract fun setAttempt(attempt: Attempt?)
    abstract fun setSubmission(submission: Submission?)
    abstract fun createReply(): Reply
}