package org.stepic.droid.core

import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Submission

interface ILessonSessionManager {
    fun restoreSubmissionForStep(stepId: Long): Submission?

    fun restoreAttemptForStep(stepId: Long): Attempt?

    fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?)

    fun reset()
}
