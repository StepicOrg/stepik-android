package org.stepic.droid.events.submissions;

public class FailSubmissionCreatedEvent extends SubmissionBase {
    public FailSubmissionCreatedEvent(long attemptId) {
        super(attemptId);
    }
}
