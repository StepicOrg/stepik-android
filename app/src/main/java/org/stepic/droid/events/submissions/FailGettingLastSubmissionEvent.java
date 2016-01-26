package org.stepic.droid.events.submissions;

public class FailGettingLastSubmissionEvent extends SubmissionBase {

    int tryNumber;

    public FailGettingLastSubmissionEvent(long attemptId, int tryNumber) {
        super(attemptId);
        this.tryNumber = tryNumber;
    }

    public int getTryNumber() {
        return tryNumber;
    }

    public void setTryNumber(int tryNumber) {
        this.tryNumber = tryNumber;
    }
}
