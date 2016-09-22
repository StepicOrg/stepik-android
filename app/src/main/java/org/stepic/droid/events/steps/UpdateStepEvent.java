package org.stepic.droid.events.steps;

public class UpdateStepEvent {
    final long stepId;
    private boolean isSuccessAttempt;

    public UpdateStepEvent(long stepId, boolean isSuccessAttempt) {
        this.stepId = stepId;
        this.isSuccessAttempt = isSuccessAttempt;
    }

    public long getStepId() {
        return stepId;
    }

    public boolean isSuccessAttempt() {
        return isSuccessAttempt;
    }
}
