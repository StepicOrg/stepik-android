package org.stepic.droid.events.units;

public class NotCachedUnitEvent {
    long uintId;

    public NotCachedUnitEvent(long uintId) {
        this.uintId = uintId;
    }

    public long getUintId() {
        return uintId;
    }
}
