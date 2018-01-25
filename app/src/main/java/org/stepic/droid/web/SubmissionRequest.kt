package org.stepic.droid.web

import org.stepic.droid.model.Reply
import org.stepic.droid.model.Submission

class SubmissionRequest(val submission: Submission) {
    constructor(reply: Reply, attemptId: Long) : this(Submission(reply, attemptId))
}