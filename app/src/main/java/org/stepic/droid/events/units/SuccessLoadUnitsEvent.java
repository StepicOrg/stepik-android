package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

import java.util.List;

public class SuccessLoadUnitsEvent {
    private final Section section;
    private final List<Unit> unitList;

    public SuccessLoadUnitsEvent(Section section, List<Unit> unitList) {
        this.section = section;
        this.unitList = unitList;
    }

    public Section getSection() {
        return section;
    }

    public List<Unit> getUnitList() {
        return unitList;
    }
}
