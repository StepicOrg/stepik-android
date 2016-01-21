package org.stepic.droid.events.attempts;

import org.stepic.droid.model.Attempt;

public class AttemptBaseEvent {

    private long stepId;
    private Attempt mAttempt;

    public AttemptBaseEvent(long stepId, Attempt attempt) {

        this.stepId = stepId;
        mAttempt = attempt;
    }

    public Attempt getAttempt() {
        return mAttempt;
    }


    public long getStepId() {
        return stepId;
    }
}
