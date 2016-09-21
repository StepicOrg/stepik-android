package org.stepic.droid.events.units;

public class UnitProgressUpdateEvent {

    long unitId;

    public UnitProgressUpdateEvent(long unitId) {
        this.unitId = unitId;
    }

    public long getUnitId() {
        return unitId;
    }
}
