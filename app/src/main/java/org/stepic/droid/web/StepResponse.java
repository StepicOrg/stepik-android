package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Step;

import java.util.List;

public class StepResponse extends StepicResponseBase {
    public StepResponse(Meta meta) {
        super(meta);
    }

    List<Step> steps;

    public List<Step> getSteps() {
        return steps;
    }
}
