package org.stepic.droid.events.units;

public class UnitProgressUpdateEvent {

    long mUnitId;

    public UnitProgressUpdateEvent(long unitId) {
        this.mUnitId = unitId;
    }

    public long getUnitId() {
        return mUnitId;
    }
}
