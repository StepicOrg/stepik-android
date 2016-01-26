package org.stepic.droid.web;

import org.stepic.droid.model.Reply;
import org.stepic.droid.model.Submission;

public class SubmissionRequest {
    Submission submission;

    public SubmissionRequest(Reply reply, long attemptId) {
        submission = new Submission(reply, attemptId);
    }
}
