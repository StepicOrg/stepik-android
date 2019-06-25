package org.stepik.android.view.step_quiz_choice.ui.delegate

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

// TODO Temporary placed in this package, will move later
abstract class StepQuizFormDelegate {
    abstract var isEnabled: Boolean

    abstract fun setAttempt(attempt: Attempt?)
    abstract fun setSubmission(submission: Submission?)
}