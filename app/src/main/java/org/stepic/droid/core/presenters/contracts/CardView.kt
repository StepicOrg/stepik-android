package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Submission

interface CardView {
    fun setSubmission(submission: Submission, animate: Boolean)
    fun onSubmissionConnectivityError()
    fun onSubmissionRequestError()
    fun onSubmissionLoading()

    fun setTitle(title: String?)
    fun setQuestion(html: String?)
    fun setAttempt(attempt: Attempt)
    fun setChoices(choices: List<Boolean>)
}