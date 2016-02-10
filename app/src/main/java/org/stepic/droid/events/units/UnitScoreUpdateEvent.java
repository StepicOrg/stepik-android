package org.stepic.droid.events.units;

public class UnitScoreUpdateEvent {

    long mUnitId;
    double newScore;

    public UnitScoreUpdateEvent(long unitId, double score) {
        this.mUnitId = unitId;
        newScore = score;
    }

    public double getNewScore() {
        return newScore;
    }

    public long getUnitId() {
        return mUnitId;
    }
}
