package org.stepic.droid.events.attempts;

public class FailAttemptEvent extends AttemptBaseEvent {
    public FailAttemptEvent(long stepId) {
        super(stepId, null);
    }
}

