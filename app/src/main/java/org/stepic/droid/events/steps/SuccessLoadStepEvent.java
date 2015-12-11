package org.stepic.droid.events.steps;

import org.stepic.droid.model.Step;

import java.util.List;

public class SuccessLoadStepEvent {
    private final List<Step> steps;

    public SuccessLoadStepEvent(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getSteps() {
        return steps;
    }
}
