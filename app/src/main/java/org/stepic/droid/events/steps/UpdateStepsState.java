package org.stepic.droid.events.steps;

import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;

import java.util.List;

public class UpdateStepsState {
    final Unit unit;
    final List<Step> steps;

    public UpdateStepsState(Unit unit, List<Step> steps) {
        this.unit = unit;
        this.steps = steps;
    }

    public Unit getUnit() {
        return unit;
    }
    public List<Step> getSteps() {
        return steps;
    }
}
