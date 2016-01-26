package org.stepic.droid.events.submissions;

import org.stepic.droid.web.SubmissionResponse;

public class SubmissionCreatedEvent extends SubmissionBase {
    private final SubmissionResponse mSubmissionResponse;

    public SubmissionCreatedEvent(long attemptId, SubmissionResponse submissionResponse) {
        super(attemptId);
        mSubmissionResponse = submissionResponse;
    }

    public SubmissionResponse getSubmissionResponse() {
        return mSubmissionResponse;
    }
}
