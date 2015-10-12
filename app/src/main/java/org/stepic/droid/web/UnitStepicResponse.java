package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Unit;

import java.util.List;

public class UnitStepicResponse extends StepicResponseBase {
    List<Unit> units;

    public UnitStepicResponse(Meta meta) {
        super(meta);
    }


    public List<Unit> getUnits() {
        return units;
    }
}
