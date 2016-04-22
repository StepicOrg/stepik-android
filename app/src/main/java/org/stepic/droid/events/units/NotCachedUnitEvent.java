package org.stepic.droid.events.units;

public class NotCachedUnitEvent {
    long unitId;

    public NotCachedUnitEvent(long unitId) {
        this.unitId = unitId;
    }

    public long getUnitId() {
        return unitId;
    }
}
