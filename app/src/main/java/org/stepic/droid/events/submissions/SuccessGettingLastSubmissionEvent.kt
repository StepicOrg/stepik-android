package org.stepic.droid.events.submissions

import org.stepic.droid.model.Submission

class SuccessGettingLastSubmissionEvent @JvmOverloads constructor(
        attemptId: Long,
        val submission: Submission? = null,
        val numberOfSubmissionsOnFirstPage: Int = 0) : SubmissionBase(attemptId)