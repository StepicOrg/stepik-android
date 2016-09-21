package org.stepic.droid.events.submissions

import org.stepic.droid.web.SubmissionResponse

class SubmissionCreatedEvent(attemptId: Long, val submissionResponse: SubmissionResponse) : SubmissionBase(attemptId)