package org.stepic.droid.events.steps;

public class UpdateStepEvent {
    final long stepId;

    public UpdateStepEvent(long stepId) {
        this.stepId = stepId;
    }

    public long getStepId() {
        return stepId;
    }
}
