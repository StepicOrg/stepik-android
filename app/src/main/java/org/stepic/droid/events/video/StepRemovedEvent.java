package org.stepic.droid.events.video;

public class StepRemovedEvent {
    final long stepId;

    public StepRemovedEvent(long stepId) {
        this.stepId = stepId;
    }

    public long getStepId() {
        return stepId;
    }
}
