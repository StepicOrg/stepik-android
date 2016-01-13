package org.stepic.droid.events.attemptions;

public class FailAttemptEvent extends AttemptBaseEvent {
    public FailAttemptEvent(long stepId) {
        super(stepId, null);
    }
}

