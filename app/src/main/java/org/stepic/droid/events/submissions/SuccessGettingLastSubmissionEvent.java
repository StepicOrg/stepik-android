package org.stepic.droid.events.submissions;

import org.stepic.droid.model.Submission;

public class SuccessGettingLastSubmissionEvent extends SubmissionBase {
    private final Submission mSubmission;

    public SuccessGettingLastSubmissionEvent(long attemptId, Submission submission) {
        super(attemptId);
        mSubmission = submission;
    }

    public Submission getSubmission() {
        return mSubmission;
    }
}
