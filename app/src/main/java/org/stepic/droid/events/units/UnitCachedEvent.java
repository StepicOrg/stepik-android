package org.stepic.droid.events.units;

public class UnitCachedEvent {
    long unitId;

    public UnitCachedEvent(long unitId) {
        this.unitId = unitId;
    }

    public long getUnitId() {
        return unitId;
    }
}
