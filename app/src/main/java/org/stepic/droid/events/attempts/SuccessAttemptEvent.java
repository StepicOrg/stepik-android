package org.stepic.droid.events.attempts;

import org.stepic.droid.model.Attempt;

public class SuccessAttemptEvent extends AttemptBaseEvent {
    private final boolean justCreated;

    public SuccessAttemptEvent(long stepId, Attempt attempt, boolean b) {
        super(stepId, attempt);
        justCreated = b;
    }

    public boolean isJustCreated() {
        return justCreated;
    }
}
