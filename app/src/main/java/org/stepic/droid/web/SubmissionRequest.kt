package org.stepic.droid.web

import org.stepik.android.model.learning.Submission
import org.stepik.android.model.learning.Reply

class SubmissionRequest(val submission: Submission) {
    constructor(reply: Reply, attemptId: Long) : this(Submission(reply = reply, attempt = attemptId))
}