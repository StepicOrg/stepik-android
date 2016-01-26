package org.stepic.droid.events.submissions;

public abstract class SubmissionBase {
    protected long attemptId;

    public SubmissionBase(long attemptId) {
        this.attemptId = attemptId;
    }

    public long getAttemptId() {
        return attemptId;
    }
}
