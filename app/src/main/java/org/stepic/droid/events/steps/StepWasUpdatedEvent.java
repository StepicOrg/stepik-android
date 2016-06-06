package org.stepic.droid.events.steps;

import org.stepic.droid.model.Step;

public class StepWasUpdatedEvent {
    private Step step;

    public StepWasUpdatedEvent(Step step) {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }
}
