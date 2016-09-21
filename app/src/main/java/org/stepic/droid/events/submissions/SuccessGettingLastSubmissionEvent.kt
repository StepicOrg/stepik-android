package org.stepic.droid.events.submissions

import org.stepic.droid.model.Submission

class SuccessGettingLastSubmissionEvent(attemptId: Long, val submission: Submission?) : SubmissionBase(attemptId)