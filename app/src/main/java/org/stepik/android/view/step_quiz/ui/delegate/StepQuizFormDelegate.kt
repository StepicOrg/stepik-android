package org.stepik.android.view.step_quiz.ui.delegate

import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

interface StepQuizFormDelegate {
    /**
     * If [isEnabled] == false form should block any changes from user
     */
    var isEnabled: Boolean

    fun setAttempt(attempt: Attempt)
    fun setSubmission(submission: Submission)

    /**
     * Generates reply from current form data
     */
    fun createReply(): Reply

    /**
     * Validates form for ability to create a reply
     * @returns null if validation successful or message string otherwise
     */
    fun validateForm(): String?
}