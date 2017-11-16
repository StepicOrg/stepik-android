package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Unit;

import java.util.List;

public class UnitMetaResponse extends MetaResponseBase {
    List<Unit> units;

    public UnitMetaResponse(Meta meta) {
        super(meta);
    }


    public List<Unit> getUnits() {
        return units;
    }
}
