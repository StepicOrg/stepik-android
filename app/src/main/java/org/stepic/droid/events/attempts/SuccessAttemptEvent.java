package org.stepic.droid.events.attempts;

import org.stepic.droid.model.Attempt;

public class SuccessAttemptEvent extends AttemptBaseEvent {
    public SuccessAttemptEvent(long stepId, Attempt attempt) {
        super(stepId, attempt);
    }
}
