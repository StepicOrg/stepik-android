package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission

interface QuizDelegate {
    var isEnabled: Boolean
    var actionButton: Button?

    fun onCreateView(parent: ViewGroup): View
    fun onViewCreated(view: View) {}

    fun setAttempt(attempt: Attempt?)
    fun setSubmission(submission: Submission?)
    fun createReply(): Reply
}