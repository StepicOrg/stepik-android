package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Unit;

import java.util.List;

public class UnitStepicResponse implements IStepicResponse {
    Meta meta;
    List<Unit> units;

    public Meta getMeta() {
        return meta;
    }

    public List<Unit> getUnits() {
        return units;
    }
}
